package com.airline.feedback.api.dto;

import com.airline.feedback.auth.AuthRole;

public class LoginResponse {

  private String username;
  private AuthRole role;
  private String redirectPath;

  public static LoginResponse of(String username, AuthRole role) {
    LoginResponse response = new LoginResponse();
    response.setUsername(username);
    response.setRole(role);
    response.setRedirectPath(role == AuthRole.ADMIN ? "/" : "/customer/");
    return response;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public AuthRole getRole() {
    return role;
  }

  public void setRole(AuthRole role) {
    this.role = role;
  }

  public String getRedirectPath() {
    return redirectPath;
  }

  public void setRedirectPath(String redirectPath) {
    this.redirectPath = redirectPath;
  }
}
