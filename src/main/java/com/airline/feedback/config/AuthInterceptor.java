package com.airline.feedback.config;

import com.airline.feedback.auth.AuthRole;
import com.airline.feedback.auth.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String path = request.getRequestURI();
    AuthRole role = getRole(request);

    if (path.equals("/landing") || path.equals("/landing.html")) {
      if (role == AuthRole.ADMIN) {
        response.sendRedirect("/");
        return false;
      }
      if (role == AuthRole.CUSTOMER) {
        response.sendRedirect("/customer/");
        return false;
      }
      return true;
    }

    if (path.equals("/admin-login") || path.equals("/admin-login.html")) {
      if (role == AuthRole.ADMIN) {
        response.sendRedirect("/");
        return false;
      }
      if (role == AuthRole.CUSTOMER) {
        response.sendRedirect("/customer/");
        return false;
      }
      return true;
    }

    if (path.equals("/admin-signup") || path.equals("/admin-signup.html")) {
      if (role == AuthRole.ADMIN) {
        response.sendRedirect("/");
        return false;
      }
      if (role == AuthRole.CUSTOMER) {
        response.sendRedirect("/customer/");
        return false;
      }
      return true;
    }

    if (path.equals("/customer/login") || path.equals("/customer/login.html")) {
      if (role == AuthRole.CUSTOMER) {
        response.sendRedirect("/customer/");
        return false;
      }
      if (role == AuthRole.ADMIN) {
        response.sendRedirect("/");
        return false;
      }
      return true;
    }

    if (path.equals("/customer/signup") || path.equals("/customer/signup.html")) {
      if (role == AuthRole.CUSTOMER) {
        response.sendRedirect("/customer/");
        return false;
      }
      if (role == AuthRole.ADMIN) {
        response.sendRedirect("/");
        return false;
      }
      return true;
    }

    if (path.startsWith("/api/")) {
      return handleApiAuth(request, response, path, role);
    }

    if (path.equals("/index.html") || path.equals("/")) {
      if (role == AuthRole.ADMIN) {
        return true;
      }
      if (role == AuthRole.CUSTOMER) {
        response.sendRedirect("/customer/");
        return false;
      }
      response.sendRedirect("/landing");
      return false;
    }

    if (path.equals("/customer") || path.equals("/customer/") || path.equals("/customer/index.html")) {
      if (role == AuthRole.CUSTOMER) {
        return true;
      }
      response.sendRedirect("/customer/login.html");
      return false;
    }

    if (path.startsWith("/customer/")
        && !path.equals("/customer/login.html")
        && !path.equals("/customer/signup.html")) {
      if (role == AuthRole.CUSTOMER) {
        return true;
      }
      response.sendRedirect("/customer/login.html");
      return false;
    }

    return true;
  }

  private boolean handleApiAuth(HttpServletRequest request, HttpServletResponse response, String path, AuthRole role) throws IOException {
    if (path.startsWith("/api/auth/")) {
      return true;
    }

    if (role == null) {
      return writeUnauthorized(response, "Login required");
    }

    if (isAdminOnlyApi(request, path) && role != AuthRole.ADMIN) {
      return writeForbidden(response, "Admin access required");
    }

    return true;
  }

  private boolean isAdminOnlyApi(HttpServletRequest request, String path) {
    if (path.startsWith("/api/analytics/")) {
      return true;
    }
    if (path.equals("/api/export/excel")) {
      return true;
    }
    if (path.matches("^/api/cases/[^/]+/status$")) {
      return true;
    }
    if (path.matches("^/api/cases/[^/]+/resolution$")) {
      return true;
    }
    return "DELETE".equalsIgnoreCase(request.getMethod()) && path.matches("^/api/cases/[^/]+$");
  }

  private AuthRole getRole(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }

    Object roleValue = session.getAttribute(AuthSession.ROLE);
    if (!(roleValue instanceof String roleName)) {
      return null;
    }

    try {
      return AuthRole.valueOf(roleName);
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }

  private boolean writeUnauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"message\":\"" + escapeJson(message) + "\"}");
    return false;
  }

  private boolean writeForbidden(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"message\":\"" + escapeJson(message) + "\"}");
    return false;
  }

  private String escapeJson(String value) {
    return value.replace("\"", "\\\"");
  }
}
