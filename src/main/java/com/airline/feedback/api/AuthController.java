package com.airline.feedback.api;

import com.airline.feedback.api.dto.LoginRequest;
import com.airline.feedback.api.dto.LoginResponse;
import com.airline.feedback.api.dto.RegisterRequest;
import com.airline.feedback.auth.AuthRole;
import com.airline.feedback.auth.AuthSession;
import com.airline.feedback.model.AuthUser;
import com.airline.feedback.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
    AuthRole role = request.getPortal();
    AuthUser user = authService.authenticateAndGetUser(role, request.getUsername(), request.getPassword()).orElse(null);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorBody("Invalid username or password"));
    }

    HttpSession session = servletRequest.getSession(true);
    session.setAttribute(AuthSession.ROLE, role.name());
    session.setAttribute(AuthSession.USERNAME, user.getUsername());
    session.setAttribute(AuthSession.EMAIL, user.getEmail());
    return ResponseEntity.ok(LoginResponse.of(user.getUsername(), user.getEmail(), role));
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
    AuthUser created = authService.register(
        request.getPortal(),
        request.getUsername(),
        request.getEmail(),
        request.getPassword()
    );

    // Auto-login after successful registration.
    HttpSession session = servletRequest.getSession(true);
    session.setAttribute(AuthSession.ROLE, created.getRole().name());
    session.setAttribute(AuthSession.USERNAME, created.getUsername());
    session.setAttribute(AuthSession.EMAIL, created.getEmail());

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(LoginResponse.of(created.getUsername(), created.getEmail(), created.getRole()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorBody("Not authenticated"));
    }

    Object roleObj = session.getAttribute(AuthSession.ROLE);
    Object usernameObj = session.getAttribute(AuthSession.USERNAME);
    Object emailObj = session.getAttribute(AuthSession.EMAIL);
    if (!(roleObj instanceof String roleValue) || !(usernameObj instanceof String usernameValue)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorBody("Not authenticated"));
    }

    AuthRole role = AuthRole.valueOf(roleValue);
    if (emailObj instanceof String emailValue && !emailValue.isBlank()) {
      return ResponseEntity.ok(LoginResponse.of(usernameValue, emailValue, role));
    }

    AuthUser user = authService.findByRoleAndIdentifier(role, usernameValue).orElse(null);
    if (user != null) {
      return ResponseEntity.ok(LoginResponse.of(user.getUsername(), user.getEmail(), role));
    }
    return ResponseEntity.ok(LoginResponse.of(usernameValue, null, role));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorBody> validationError(MethodArgumentNotValidException ex) {
    var fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
    String message = (fieldError != null && fieldError.getDefaultMessage() != null)
        ? fieldError.getDefaultMessage()
        : "Validation failed";
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorBody(message));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorBody> badRequest(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorBody(ex.getMessage()));
  }

  public static class ErrorBody {
    private final String message;

    public ErrorBody(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
