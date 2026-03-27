package com.airline.feedback.service;

import com.airline.feedback.auth.AuthRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final String adminUsername;
  private final String adminPassword;
  private final String customerUsername;
  private final String customerPassword;

  public AuthService(
      @Value("${app.auth.admin.username:admin}") String adminUsername,
      @Value("${app.auth.admin.password:admin123}") String adminPassword,
      @Value("${app.auth.customer.username:customer}") String customerUsername,
      @Value("${app.auth.customer.password:customer123}") String customerPassword
  ) {
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
    this.customerUsername = customerUsername;
    this.customerPassword = customerPassword;
  }

  public boolean authenticate(AuthRole role, String username, String password) {
    if (role == AuthRole.ADMIN) {
      return adminUsername.equals(username) && adminPassword.equals(password);
    }
    return customerUsername.equals(username) && customerPassword.equals(password);
  }
}
