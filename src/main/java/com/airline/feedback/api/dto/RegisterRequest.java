package com.airline.feedback.api.dto;

import com.airline.feedback.auth.AuthRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

  @NotBlank(message = "Username is required")
  @Size(max = 60, message = "Username must be <= 60 chars")
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Email is invalid")
  @Size(max = 160, message = "Email must be <= 160 chars")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 100, message = "Password must be 6-100 chars")
  private String password;

  @NotNull(message = "Portal is required")
  private AuthRole portal;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public AuthRole getPortal() {
    return portal;
  }

  public void setPortal(AuthRole portal) {
    this.portal = portal;
  }
}