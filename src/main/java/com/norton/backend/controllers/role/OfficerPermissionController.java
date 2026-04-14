package com.norton.backend.controllers.role;

import com.norton.backend.dto.responses.OfficerPermissionResponse;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.services.officer.OfficerService;
import com.norton.backend.services.role.OfficerPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(OfficerPermissionController.BASE_URL)
public class OfficerPermissionController {

  public static final String BASE_URL = ("/api/v1/officer-permissions");
  private final OfficerPermissionService officerPermissionService;
  private final OfficerService officerService;

  @GetMapping
  @PreAuthorize("hasAuthority('MANAGE_ROLES')")
  public ResponseEntity<PageResponse<OfficerPermissionResponse>> getAll(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(officerPermissionService.getAllPermissions(page, size));
  }
}
