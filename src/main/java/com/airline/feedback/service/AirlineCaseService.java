package com.airline.feedback.service;

import com.airline.feedback.model.AirlineCase;
import com.airline.feedback.model.CaseStatus;
import com.airline.feedback.model.CaseType;
import com.airline.feedback.repo.AirlineCaseRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AirlineCaseService {

  private final AirlineCaseRepository repository;
  private final TicketService ticketService;
  private final TriageService triageService;

  public AirlineCaseService(AirlineCaseRepository repository, TicketService ticketService, TriageService triageService) {
    this.repository = repository;
    this.ticketService = ticketService;
    this.triageService = triageService;
  }

  @Transactional
  public AirlineCase createFeedbackOrComplaint(CaseType type,
                                                 String customerName,
                                                 String customerEmail,
                                                 String flightNumber,
                                                 LocalDate journeyDate,
                                                 String contactChannel,
                                                 String message) {
    String ticketNumber = ticketService.nextTicketNumber();

    TriageService.TriageResult triage = triageService.triage(message);

    Instant now = Instant.now();
    CaseStatus status = CaseStatus.SUBMITTED;

    AirlineCase airlineCase = new AirlineCase(
        ticketNumber,
        type,
        status,
        customerName,
        customerEmail,
        flightNumber,
        journeyDate,
        contactChannel,
        message,
        triage.getCategory(),
        triage.getSentimentScore(),
        triage.getPriority(),
        null,
        triage.getResolutionSuggestion(),
        now,
        now
    );

    return repository.save(airlineCase);
  }

  public TriageService.TriageResult previewTriage(String message) {
    return triageService.triage(message);
  }

  public AirlineCase getById(String id) {
    return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Case not found"));
  }

  public AirlineCase getByTicketNumber(String ticketNumber) {
    return repository.findByTicketNumber(ticketNumber)
        .orElseThrow(() -> new NoSuchElementException("Case not found"));
  }

  public List<AirlineCase> getLatestByEmail(String email, int limit) {
    return repository.findByCustomerEmailOrderByCreatedAtDesc(email, PageRequest.of(0, limit));
  }

  @Transactional
  public AirlineCase updateStatus(String id, CaseStatus newStatus, String assignedAgent) {
    AirlineCase c = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Case not found"));
    Instant now = Instant.now();

    String agent = StringUtils.hasText(assignedAgent) ? assignedAgent.trim() : null;
    c.updateStatus(newStatus, agent, now);
    return repository.save(c);
  }

  @Transactional
  public AirlineCase addResolution(String id, String resolutionText) {
    if (!StringUtils.hasText(resolutionText)) {
      throw new IllegalArgumentException("resolutionText is required");
    }
    AirlineCase c = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Case not found"));
    Instant now = Instant.now();
    c.addResolution(resolutionText.trim(), now);
    if (c.getStatus() != CaseStatus.RESOLVED) {
      c.updateStatus(CaseStatus.RESOLVED, c.getAssignedAgent(), now);
    }
    return repository.save(c);
  }
}

