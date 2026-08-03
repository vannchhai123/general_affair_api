package com.norton.backend.controllers.permission;

import com.norton.backend.dto.request.OfficerPermissionRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.officers.OfficerPermissionResponse;
import com.norton.backend.services.role.OfficerPermissionService;
import com.norton.backend.services.role.PermissionService;
import com.norton.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(OfficerPermissionController.BASE_URL)
public class OfficerPermissionController {

  public static final String BASE_URL = ("/api/v1/officer-permissions");
  private final OfficerPermissionService officerPermissionService;
  private final PermissionService permissionService;
  private final SecurityUtils securityUtils;

  @GetMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_VIEW_PERMISSION)")
  public ResponseEntity<PageResponse<OfficerPermissionResponse>> getAll(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(officerPermissionService.getAllPermissions(page, size));
  }

  @PostMapping
  @PreAuthorize(
      "hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_ASSIGN_PERMISSION)")
  public ResponseEntity<OfficerPermissionResponse> create(
      @Valid @RequestBody OfficerPermissionRequest request) {

    Long grantedBy = securityUtils.getCurrentUserId();

    OfficerPermissionResponse response = permissionService.create(request, grantedBy);

    return ResponseEntity.ok(response);
  }

  @PutMapping("/officers/{officerId}/role")
  @PreAuthorize(
      "hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_ASSIGN_PERMISSION) or hasRole('ADMIN')")
  public ResponseEntity<java.util.Map<String, Object>> assignRoleToOfficer(
      @PathVariable Long officerId, @RequestBody java.util.Map<String, String> body) {
    String roleName = body.get("roleName") != null ? body.get("roleName") : body.get("role");
    if (roleName == null || roleName.isBlank()) {
      throw new com.norton.backend.exceptions.BadRequestException("roleName is required");
    }
    permissionService.assignRoleToOfficer(officerId, roleName.trim());
    return ResponseEntity.ok(
        java.util.Map.of("success", true, "message", "Officer role assigned successfully"));
  }
}
