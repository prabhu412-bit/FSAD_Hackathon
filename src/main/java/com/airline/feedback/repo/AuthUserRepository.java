package com.airline.feedback.repo;

import com.airline.feedback.auth.AuthRole;
import com.airline.feedback.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, String> {

  Optional<AuthUser> findByRoleAndUsernameIgnoreCase(AuthRole role, String username);

  Optional<AuthUser> findByRoleAndEmailIgnoreCase(AuthRole role, String email);

  boolean existsByRoleAndUsernameIgnoreCase(AuthRole role, String username);

  boolean existsByRoleAndEmailIgnoreCase(AuthRole role, String email);
}