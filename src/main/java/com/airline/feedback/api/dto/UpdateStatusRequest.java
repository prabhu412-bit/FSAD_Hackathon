package com.airline.feedback.api.dto;

import com.airline.feedback.model.CaseStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateStatusRequest {

  @NotNull
  private CaseStatus status;

  @Size(max = 80)
  private String assignedAgent;

  public CaseStatus getStatus() {
    return status;
  }

  public void setStatus(CaseStatus status) {
    this.status = status;
  }

  public String getAssignedAgent() {
    return assignedAgent;
  }

  public void setAssignedAgent(String assignedAgent) {
    this.assignedAgent = assignedAgent;
  }
}

