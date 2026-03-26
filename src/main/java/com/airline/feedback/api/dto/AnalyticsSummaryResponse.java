package com.airline.feedback.api.dto;

import java.util.Map;

public class AnalyticsSummaryResponse {

  private long totalCases;
  private long feedbackCount;
  private long complaintCount;
  private long resolvedCount;
  private long openCount;
  private long highPriorityCount;
  private long last7DaysCount;

  // status -> count (for quick bar visualization)
  private Map<String, Long> statusCounts;

  public long getTotalCases() {
    return totalCases;
  }

  public void setTotalCases(long totalCases) {
    this.totalCases = totalCases;
  }

  public long getFeedbackCount() {
    return feedbackCount;
  }

  public void setFeedbackCount(long feedbackCount) {
    this.feedbackCount = feedbackCount;
  }

  public long getComplaintCount() {
    return complaintCount;
  }

  public void setComplaintCount(long complaintCount) {
    this.complaintCount = complaintCount;
  }

  public long getResolvedCount() {
    return resolvedCount;
  }

  public void setResolvedCount(long resolvedCount) {
    this.resolvedCount = resolvedCount;
  }

  public long getOpenCount() {
    return openCount;
  }

  public void setOpenCount(long openCount) {
    this.openCount = openCount;
  }

  public long getHighPriorityCount() {
    return highPriorityCount;
  }

  public void setHighPriorityCount(long highPriorityCount) {
    this.highPriorityCount = highPriorityCount;
  }

  public long getLast7DaysCount() {
    return last7DaysCount;
  }

  public void setLast7DaysCount(long last7DaysCount) {
    this.last7DaysCount = last7DaysCount;
  }

  public Map<String, Long> getStatusCounts() {
    return statusCounts;
  }

  public void setStatusCounts(Map<String, Long> statusCounts) {
    this.statusCounts = statusCounts;
  }
}

