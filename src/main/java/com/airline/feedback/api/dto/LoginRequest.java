package com.airline.feedback.api.dto;

import com.airline.feedback.auth.AuthRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {

  @NotBlank(message = "Username is required")
  private String username;

  @NotBlank(message = "Password is required")
  private String password;

  @NotNull(message = "Portal is required")
  private AuthRole portal;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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
