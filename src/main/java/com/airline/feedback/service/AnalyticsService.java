package com.airline.feedback.service;

import com.airline.feedback.api.dto.AnalyticsSummaryResponse;
import com.airline.feedback.model.AirlineCase;
import com.airline.feedback.model.CaseStatus;
import com.airline.feedback.model.CaseType;
import com.airline.feedback.repo.AirlineCaseRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class AnalyticsService {

  private final AirlineCaseRepository repository;

  public AnalyticsService(AirlineCaseRepository repository) {
    this.repository = repository;
  }

  public AnalyticsSummaryResponse summary() {
    Instant now = Instant.now();
    Instant last7 = now.minus(Duration.ofDays(7));

    long total = repository.count();
    long feedback = repository.countByType(CaseType.FEEDBACK);
    long complaint = repository.countByType(CaseType.COMPLAINT);
    long resolved = repository.countByStatus(CaseStatus.RESOLVED);
    long open = total - resolved;
    long highPriority = repository.countByPriorityGreaterThanEqual(4);
    long last7Days = repository.countByCreatedAtAfter(last7);

    AnalyticsSummaryResponse res = new AnalyticsSummaryResponse();
    res.setTotalCases(total);
    res.setFeedbackCount(feedback);
    res.setComplaintCount(complaint);
    res.setResolvedCount(resolved);
    res.setOpenCount(open);
    res.setHighPriorityCount(highPriority);
    res.setLast7DaysCount(last7Days);

    res.setStatusCounts(Map.of(
        CaseStatus.SUBMITTED.name(), repository.countByStatus(CaseStatus.SUBMITTED),
        CaseStatus.TRIAGED.name(), repository.countByStatus(CaseStatus.TRIAGED),
        CaseStatus.IN_PROGRESS.name(), repository.countByStatus(CaseStatus.IN_PROGRESS),
        CaseStatus.RESOLVED.name(), resolved
    ));

    // Category breakdown
    String[] categories = {"Flight Disruption", "Baggage/Handling", "Billing/Refund", "Service Quality", "General Feedback"};
    java.util.Map<String, Long> catCounts = new java.util.LinkedHashMap<>();
    for (String cat : categories) {
      catCounts.put(cat, repository.countByCategory(cat));
    }
    res.setCategoryCounts(catCounts);

    // Type breakdown
    res.setTypeCounts(Map.of(
        CaseType.FEEDBACK.name(), feedback,
        CaseType.COMPLAINT.name(), complaint
    ));

    return res;
  }

  public java.util.List<AirlineCase> recent(int limit) {
    return repository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
  }
}

