package com.airline.feedback.api;

import com.airline.feedback.auth.AuthRole;
import com.airline.feedback.auth.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerPageController {

  @GetMapping("/")
  public String index(HttpServletRequest request) {
    if (readRole(request) != AuthRole.ADMIN) {
      return "redirect:/admin-login.html";
    }
    return "forward:/index.html";
  }

  // Make `/customer` work (friendly mode) instead of requiring `/customer/index.html`.
  @GetMapping({"/customer", "/customer/"})
  public String customer(HttpServletRequest request) {
    if (readRole(request) != AuthRole.CUSTOMER) {
      return "redirect:/customer/login.html";
    }
    return "forward:/customer/index.html";
  }

  @GetMapping("/admin-login")
  public String adminLogin() {
    return "forward:/admin-login.html";
  }

  @GetMapping("/customer/login")
  public String customerLogin() {
    return "forward:/customer/login.html";
  }

  private AuthRole readRole(HttpServletRequest request) {
    var session = request.getSession(false);
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
}

