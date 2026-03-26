package com.airline.feedback.api;

import com.airline.feedback.api.dto.AddResolutionRequest;
import com.airline.feedback.api.dto.AirlineCaseResponse;
import com.airline.feedback.api.dto.CreateComplaintRequest;
import com.airline.feedback.api.dto.CreateFeedbackRequest;
import com.airline.feedback.api.dto.TriagePreviewRequest;
import com.airline.feedback.api.dto.TriagePreviewResponse;
import com.airline.feedback.api.dto.UpdateStatusRequest;
import com.airline.feedback.model.CaseStatus;
import com.airline.feedback.model.CaseType;
import com.airline.feedback.model.AirlineCase;
import com.airline.feedback.service.AirlineCaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AirlineCaseController {

  private final AirlineCaseService service;

  public AirlineCaseController(AirlineCaseService service) {
    this.service = service;
  }

  @PostMapping("/feedback")
  public ResponseEntity<AirlineCaseResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest req) {
    AirlineCase created = service.createFeedbackOrComplaint(
        CaseType.FEEDBACK,
        req.getCustomerName(),
        req.getCustomerEmail(),
        req.getFlightNumber(),
        req.getJourneyDate(),
        req.getContactChannel(),
        req.getMessage()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(AirlineCaseResponse.from(created));
  }

  @PostMapping("/complaints")
  public ResponseEntity<AirlineCaseResponse> createComplaint(@Valid @RequestBody CreateComplaintRequest req) {
    AirlineCase created = service.createFeedbackOrComplaint(
        CaseType.COMPLAINT,
        req.getCustomerName(),
        req.getCustomerEmail(),
        req.getFlightNumber(),
        req.getJourneyDate(),
        req.getContactChannel(),
        req.getMessage()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(AirlineCaseResponse.from(created));
  }

  @PostMapping("/triage/preview")
  public ResponseEntity<TriagePreviewResponse> triagePreview(@Valid @RequestBody TriagePreviewRequest req) {
    var result = service.previewTriage(req.getMessage());
    TriagePreviewResponse r = new TriagePreviewResponse();
    r.setCategory(result.getCategory());
    r.setSentimentScore(result.getSentimentScore());
    r.setPriority(result.getPriority());
    r.setTriageSuggestion(result.getResolutionSuggestion());
    return ResponseEntity.ok(r);
  }

  @GetMapping("/cases/lookup")
  public ResponseEntity<AirlineCaseResponse> lookupByTicket(@RequestParam("ticketNumber") String ticketNumber) {
    AirlineCase found = service.getByTicketNumber(ticketNumber);
    return ResponseEntity.ok(AirlineCaseResponse.from(found));
  }

  @GetMapping("/cases/my")
  public ResponseEntity<List<AirlineCaseResponse>> latestForEmail(
      @RequestParam("email") String email,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit
  ) {
    if (limit < 1 || limit > 25) limit = 10;

    List<AirlineCaseResponse> res = service.getLatestByEmail(email, limit)
        .stream()
        .map(AirlineCaseResponse::from)
        .collect(Collectors.toList());
    return ResponseEntity.ok(res);
  }

  @PatchMapping("/cases/{id}/status")
  public ResponseEntity<AirlineCaseResponse> updateStatus(
      @PathVariable("id") String id,
      @Valid @RequestBody UpdateStatusRequest req
  ) {
    CaseStatus status = req.getStatus();
    AirlineCase updated = service.updateStatus(id, status, req.getAssignedAgent());
    return ResponseEntity.ok(AirlineCaseResponse.from(updated));
  }

  @PostMapping("/cases/{id}/resolution")
  public ResponseEntity<AirlineCaseResponse> addResolution(
      @PathVariable("id") String id,
      @Valid @RequestBody AddResolutionRequest req
  ) {
    AirlineCase updated = service.addResolution(id, req.getResolutionText());
    return ResponseEntity.ok(AirlineCaseResponse.from(updated));
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<?> notFound(NoSuchElementException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> badRequest(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorBody(ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> validationError(MethodArgumentNotValidException ex) {
    var fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
    String message = (fieldError != null && fieldError.getDefaultMessage() != null)
        ? fieldError.getDefaultMessage()
        : "Validation failed";
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorBody(message));
  }

  public static class ErrorBody {
    private final String message;

    public ErrorBody(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}

