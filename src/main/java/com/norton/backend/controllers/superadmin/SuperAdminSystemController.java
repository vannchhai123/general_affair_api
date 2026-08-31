package com.norton.backend.controllers.superadmin;

import com.norton.backend.dto.responses.superadmin.SystemHealthResponse;
import com.norton.backend.services.superadmin.SuperAdminSystemService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SuperAdminSystemController.BASE_URL)
public class SuperAdminSystemController {

  public static final String BASE_URL = "/api/v1/super-admin/system";

  private final SuperAdminSystemService systemService;

  @GetMapping("/health")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<SystemHealthResponse> getSystemHealth() {
    return ResponseEntity.ok(systemService.getSystemHealth());
  }

  @PostMapping("/clear-cache")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<Map<String, Object>> clearCache() {
    return ResponseEntity.ok(systemService.clearCache());
  }
}
