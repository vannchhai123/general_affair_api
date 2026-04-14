package com.norton.backend.controllers.role;

import com.norton.backend.dto.request.PermissionRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.PermissionResponse;
import com.norton.backend.services.role.PermissionService;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(PermissionController.BASE_URL)
public class PermissionController {

  public static final String BASE_URL = "/api/v1/permissions";
  private final PermissionService permissionService;

  @PostMapping("/roles/{role}/assign")
  @PreAuthorize("hasAuthority('MANAGE_ROLES')")
  public ResponseEntity<Map<String, String>> assignPermissionsToRole(
      @PathVariable String role, @RequestBody PermissionRequest request) {

    System.out.println("PermissionName: " + request.getPermissionName());
    permissionService.assignPermissionsToRole(
        role, Collections.singletonList(request.getPermissionName()));
    return ResponseEntity.ok(Map.of("message", "Permissions assigned successfully"));
  }

  @PostMapping
  @PreAuthorize("hasAuthority('MANAGE_ROLES')")
  public ResponseEntity<PermissionResponse> createPermission(
      @RequestBody PermissionRequest request) {
    PermissionResponse response = permissionService.createPermission(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{role}/permissions")
  @PreAuthorize("hasAuthority('MANAGE_ROLES')")
  public ResponseEntity<PageResponse<PermissionResponse>> getPermissionsByRole(
      @RequestParam(required = false) String category,
      @PageableDefault(size = 10) Pageable pageable) {

    return ResponseEntity.ok(permissionService.getPermissions(category, pageable));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('MANAGE_ROLES')")
  public ResponseEntity<PermissionResponse> updatePermission(
      @PathVariable Long id, @RequestBody PermissionRequest request) {
    PermissionResponse response = permissionService.updatePermission(id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('MANAGE_ROLES')")
  public ResponseEntity<Map<String, Boolean>> deletePermission(@PathVariable Long id) {
    permissionService.deletePermission(id);
    return ResponseEntity.ok(Map.of("success", true));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('MANAGE_ROLES')")
  public ResponseEntity<PageResponse<PermissionResponse>> getPermissions(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String keyword,
      Pageable pageable) {
    return ResponseEntity.ok(permissionService.getPermissions(category, keyword, pageable));
  }
}
