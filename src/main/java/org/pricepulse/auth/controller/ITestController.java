package org.pricepulse.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test")
public class ITestController {

  @GetMapping("/public")
  public String publicEndpoint() {
    return "Public endpoint";
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin")
  public String adminEndpoint() {
    return "Admin access granted";
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/user")
  public String userEndpoint() {
    return "User access granted";
  }
}
