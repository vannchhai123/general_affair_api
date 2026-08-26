package com.norton.backend.controllers.role;

import com.norton.backend.dto.request.role.CreateRoleRequest;
import com.norton.backend.dto.request.role.SyncRolePermissionsRequest;
import com.norton.backend.dto.request.role.UpdateRoleRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.role.RoleResponse;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.models.UserModel;
import com.norton.backend.services.role.RoleService;
import com.norton.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(RoleController.BASE_URL)
public class RoleController {

  public static final String BASE_URL = "/api/v1/roles";

  private final RoleService roleService;
  private final SecurityUtils securityUtils;

  @GetMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ROLE_VIEW)")
  public ResponseEntity<PageResponse<RoleResponse>> getAllRoles(
      @RequestParam(required = false) String keyword,
      @PageableDefault(size = 10, sort = "hierarchyLevel", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return ResponseEntity.ok(roleService.getAllRoles(keyword, pageable));
  }

  @GetMapping("/simple")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<RoleSimpleResponse>> getSimpleRoles() {
    return ResponseEntity.ok(roleService.getSimpleRoles());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ROLE_VIEW)")
  public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
    return ResponseEntity.ok(roleService.getRoleById(id));
  }

  @PostMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ROLE_CREATE)")
  public ResponseEntity<RoleResponse> createRole(
      @Valid @RequestBody CreateRoleRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    RoleResponse response = roleService.createRole(request, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ROLE_UPDATE)")
  public ResponseEntity<RoleResponse> updateRole(
      @PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    RoleResponse response = roleService.updateRole(id, request, currentUser);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ROLE_DELETE)")
  public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
    UserModel currentUser = securityUtils.getCurrentUser();
    roleService.deleteRole(id, currentUser);
    return ResponseEntity.ok(
        Map.of("success", true, "message", "Role deleted successfully", "id", id));
  }

  @PutMapping("/{id}/permissions")
  @PreAuthorize(
      "hasAuthority(T(com.norton.backend.security.Permissions).ROLE_ASSIGN_PERMISSION)")
  public ResponseEntity<Map<String, Object>> syncRolePermissions(
      @PathVariable Long id, @Valid @RequestBody SyncRolePermissionsRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    Map<String, Object> response =
        roleService.syncRolePermissions(id, request.getPermissionIds(), currentUser);
    return ResponseEntity.ok(response);
  }
}
