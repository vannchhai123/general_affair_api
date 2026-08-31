package com.norton.backend.controllers.superadmin;

import com.norton.backend.dto.responses.superadmin.SuperAdminStatsResponse;
import com.norton.backend.services.superadmin.SuperAdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SuperAdminDashboardController.BASE_URL)
public class SuperAdminDashboardController {

  public static final String BASE_URL = "/api/v1/super-admin/dashboard";

  private final SuperAdminDashboardService superAdminDashboardService;

  @GetMapping("/stats")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', T(com.norton.backend.security.Permissions).DASHBOARD_VIEW)")
  public ResponseEntity<SuperAdminStatsResponse> getSystemStats() {
    return ResponseEntity.ok(superAdminDashboardService.getSystemStats());
  }
}
