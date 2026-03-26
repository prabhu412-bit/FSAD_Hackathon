package com.airline.feedback.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "airline_cases")
public class AirlineCase {

  @Id
  @Column(length = 36)
  private String id = java.util.UUID.randomUUID().toString();

  @Column(nullable = false, unique = true, length = 32)
  private String ticketNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private CaseType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private CaseStatus status;

  @Column(nullable = false, length = 80)
  private String customerName;

  @Column(nullable = false, length = 160)
  private String customerEmail;

  @Column(length = 32)
  private String flightNumber;

  private LocalDate journeyDate;

  @Column(length = 32)
  private String contactChannel; // web, app, kiosk, call-center

  @Column(nullable = false, length = 5000)
  private String message;

  @Column(length = 80)
  private String category;

  // -5 (very negative) ... +5 (very positive)
  private int sentimentScore;

  // 1 (low) ... 5 (urgent)
  private int priority;

  @Column(length = 80)
  private String assignedAgent;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @Column(length = 4000)
  private String resolutionText;

  @Column(length = 2000)
  private String triageSuggestion;

  private Instant resolvedAt;

  @SuppressWarnings("unused")
  protected AirlineCase() {
    // JPA
  }

  public AirlineCase(String ticketNumber,
                      CaseType type,
                      CaseStatus status,
                      String customerName,
                      String customerEmail,
                      String flightNumber,
                      LocalDate journeyDate,
                      String contactChannel,
                      String message,
                      String category,
                      int sentimentScore,
                      int priority,
                      String assignedAgent,
                      String triageSuggestion,
                      Instant createdAt,
                      Instant updatedAt) {
    this.ticketNumber = ticketNumber;
    this.type = type;
    this.status = status;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.flightNumber = flightNumber;
    this.journeyDate = journeyDate;
    this.contactChannel = contactChannel;
    this.message = message;
    this.category = category;
    this.sentimentScore = sentimentScore;
    this.priority = priority;
    this.assignedAgent = assignedAgent;
    this.triageSuggestion = triageSuggestion;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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

  public String getResolutionText() {
    return resolutionText;
  }

  public String getTriageSuggestion() {
    return triageSuggestion;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public void updateStatus(CaseStatus newStatus, String newAssignedAgent, Instant now) {
    this.status = newStatus;
    if (newAssignedAgent != null && !newAssignedAgent.isBlank()) {
      this.assignedAgent = newAssignedAgent;
    }
    this.updatedAt = now;
  }

  public void addResolution(String resolutionText, Instant now) {
    this.resolutionText = resolutionText;
    this.resolvedAt = now;
    this.updatedAt = now;
  }
}

