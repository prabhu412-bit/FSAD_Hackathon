package com.airline.feedback.model;

import com.airline.feedback.auth.AuthRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "auth_users")
public class AuthUser {

  @Id
  @Column(length = 36)
  private String id = java.util.UUID.randomUUID().toString();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private AuthRole role;

  @Column(nullable = false, length = 60)
  private String username;

  @Column(nullable = false, length = 160)
  private String email;

  @Column(nullable = false, length = 120)
  private String passwordHash;

  @Column(nullable = false)
  private Instant createdAt;

  @SuppressWarnings("unused")
  protected AuthUser() {
    // JPA
  }

  public AuthUser(AuthRole role, String username, String email, String passwordHash, Instant createdAt) {
    this.role = role;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public AuthRole getRole() {
    return role;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}