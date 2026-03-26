package com.airline.feedback.api;

import com.airline.feedback.api.dto.AnalyticsSummaryResponse;
import com.airline.feedback.api.dto.AirlineCaseResponse;
import com.airline.feedback.model.AirlineCase;
import com.airline.feedback.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  public AnalyticsController(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @GetMapping("/summary")
  public ResponseEntity<AnalyticsSummaryResponse> summary() {
    return ResponseEntity.ok(analyticsService.summary());
  }

  @GetMapping("/recent")
  public ResponseEntity<List<AirlineCaseResponse>> recent(@RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
    if (limit < 1) limit = 1;
    if (limit > 50) limit = 50;

    List<AirlineCase> cases = analyticsService.recent(limit);
    return ResponseEntity.ok(cases.stream().map(AirlineCaseResponse::from).collect(Collectors.toList()));
  }
}

