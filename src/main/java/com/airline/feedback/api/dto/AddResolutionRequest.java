package com.airline.feedback.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddResolutionRequest {

  @NotBlank
  @Size(max = 4000)
  private String resolutionText;

  public String getResolutionText() {
    return resolutionText;
  }

  public void setResolutionText(String resolutionText) {
    this.resolutionText = resolutionText;
  }
}

