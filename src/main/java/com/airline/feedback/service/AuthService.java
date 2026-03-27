package com.airline.feedback.service;

import com.airline.feedback.auth.AuthRole;
import com.airline.feedback.model.AuthUser;
import com.airline.feedback.repo.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
public class AuthService {

  private final AuthUserRepository authUserRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
    this.authUserRepository = authUserRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public boolean authenticate(AuthRole role, String username, String password) {
    if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || role == null) {
      return false;
    }

    String identifier = username.trim();
    AuthUser user = authUserRepository.findByRoleAndUsernameIgnoreCase(role, identifier)
        .or(() -> authUserRepository.findByRoleAndEmailIgnoreCase(role, identifier))
        .orElse(null);

    return user != null && passwordEncoder.matches(password, user.getPasswordHash());
  }

  public AuthUser register(AuthRole role, String username, String email, String password) {
    if (role == null) {
      throw new IllegalArgumentException("Portal role is required");
    }
    if (!StringUtils.hasText(username)) {
      throw new IllegalArgumentException("Username is required");
    }
    if (!StringUtils.hasText(email)) {
      throw new IllegalArgumentException("Email is required");
    }
    if (!StringUtils.hasText(password) || password.length() < 6) {
      throw new IllegalArgumentException("Password must be at least 6 characters");
    }

    String normalizedUsername = username.trim();
    String normalizedEmail = email.trim().toLowerCase();

    if (authUserRepository.existsByRoleAndUsernameIgnoreCase(role, normalizedUsername)) {
      throw new IllegalArgumentException("Username already exists for this portal");
    }
    if (authUserRepository.existsByRoleAndEmailIgnoreCase(role, normalizedEmail)) {
      throw new IllegalArgumentException("Email already exists for this portal");
    }

    AuthUser user = new AuthUser(
        role,
        normalizedUsername,
        normalizedEmail,
        passwordEncoder.encode(password),
        Instant.now()
    );

    return authUserRepository.save(user);
  }
}
