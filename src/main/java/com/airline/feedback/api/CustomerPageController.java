package com.airline.feedback.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerPageController {

  // Make `/customer` work (friend mode) instead of requiring `/customer/index.html`.
  @GetMapping({"/customer", "/customer/"})
  public String customer() {
    return "forward:/customer/index.html";
  }
}

