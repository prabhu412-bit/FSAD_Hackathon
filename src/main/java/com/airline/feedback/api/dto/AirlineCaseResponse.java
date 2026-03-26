package com.airline.feedback.api.dto;

import com.airline.feedback.model.AirlineCase;
import com.airline.feedback.model.CaseStatus;
import com.airline.feedback.model.CaseType;

import java.time.Instant;
import java.time.LocalDate;

public class AirlineCaseResponse {
  private String id;
  private String ticketNumber;
  private CaseType type;
  private CaseStatus status;
  private String customerName;
  private String customerEmail;
  private String flightNumber;
  private LocalDate journeyDate;
  private String contactChannel;
  private String message;
  private String category;
  private int sentimentScore;
  private int priority;
  private String assignedAgent;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant resolvedAt;
  private String resolutionText;
  private String triageSuggestion;

  public static AirlineCaseResponse from(AirlineCase c) {
    AirlineCaseResponse r = new AirlineCaseResponse();
    r.id = c.getId();
    r.ticketNumber = c.getTicketNumber();
    r.type = c.getType();
    r.status = c.getStatus();
    r.customerName = c.getCustomerName();
    r.customerEmail = c.getCustomerEmail();
    r.flightNumber = c.getFlightNumber();
    r.journeyDate = c.getJourneyDate();
    r.contactChannel = c.getContactChannel();
    r.message = c.getMessage();
    r.category = c.getCategory();
    r.sentimentScore = c.getSentimentScore();
    r.priority = c.getPriority();
    r.assignedAgent = c.getAssignedAgent();
    r.createdAt = c.getCreatedAt();
    r.updatedAt = c.getUpdatedAt();
    r.resolvedAt = c.getResolvedAt();
    r.resolutionText = c.getResolutionText();
    r.triageSuggestion = c.getTriageSuggestion();
    return r;
  }

  public String getId() {
    return id;
  }

  public String getTicketNumber() {
    return ticketNumber;
  }

  public CaseType getType() {
    return type;
  }

  public CaseStatus getStatus() {
    return status;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public String getFlightNumber() {
    return flightNumber;
  }

  public LocalDate getJourneyDate() {
    return journeyDate;
  }

  public String getContactChannel() {
    return contactChannel;
  }

  public String getMessage() {
    return message;
  }

  public String getCategory() {
    return category;
  }

  public int getSentimentScore() {
    return sentimentScore;
  }

  public int getPriority() {
    return priority;
  }

  public String getAssignedAgent() {
    return assignedAgent;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public String getResolutionText() {
    return resolutionText;
  }

  public String getTriageSuggestion() {
    return triageSuggestion;
  }
}

