package com.airline.feedback.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateComplaintRequest {

  @NotBlank
  @Size(max = 80)
  private String customerName;

  @NotBlank
  @Email
  @Size(max = 160)
  private String customerEmail;

  @NotBlank
  @Pattern(regexp = "^[A-Za-z0-9\\-]{3,10}$", message = "flightNumber should look like AA123 or A1-234")
  private String flightNumber;

  @NotNull
  @PastOrPresent
  private LocalDate journeyDate;

  @NotBlank
  @Size(max = 32)
  private String contactChannel;

  @NotBlank
  @Size(max = 5000)
  private String message;

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  public String getFlightNumber() {
    return flightNumber;
  }

  public void setFlightNumber(String flightNumber) {
    this.flightNumber = flightNumber;
  }

  public LocalDate getJourneyDate() {
    return journeyDate;
  }

  public void setJourneyDate(LocalDate journeyDate) {
    this.journeyDate = journeyDate;
  }

  public String getContactChannel() {
    return contactChannel;
  }

  public void setContactChannel(String contactChannel) {
    this.contactChannel = contactChannel;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

