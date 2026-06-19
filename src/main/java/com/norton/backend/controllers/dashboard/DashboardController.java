package com.norton.backend.controllers.dashboard;

import com.norton.backend.dto.responses.dashboard.DashboardResponse;
import com.norton.backend.services.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(DashboardController.BASE_URL)
public class DashboardController {
  public static final String BASE_URL = "/api/v1/dashboard";

  private final DashboardService dashboardService;

  @GetMapping
  public ResponseEntity<DashboardResponse> getDashboard() {
    return ResponseEntity.ok(dashboardService.getDashboard());
  }
}
