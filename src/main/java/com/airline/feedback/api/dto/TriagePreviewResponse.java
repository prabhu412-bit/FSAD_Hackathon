package com.airline.feedback.api.dto;

public class TriagePreviewResponse {
  private String category;
  private int sentimentScore;
  private int priority;
  private String triageSuggestion;

  public String getCategory() {
    return category;
  }

  public int getSentimentScore() {
    return sentimentScore;
  }

  public int getPriority() {
    return priority;
  }

  public String getTriageSuggestion() {
    return triageSuggestion;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setSentimentScore(int sentimentScore) {
    this.sentimentScore = sentimentScore;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void setTriageSuggestion(String triageSuggestion) {
    this.triageSuggestion = triageSuggestion;
  }
}

