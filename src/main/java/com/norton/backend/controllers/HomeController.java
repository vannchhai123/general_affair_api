package com.norton.backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HomeController {

  @GetMapping("/")
  public String Home() {
    return "API is running";
  }

  @PreAuthorize("hasRole('HEAD_OFFICE')")
  @GetMapping("/admin/test")
  public String adminTest() {
    return "Admin access granted";
  }
}
