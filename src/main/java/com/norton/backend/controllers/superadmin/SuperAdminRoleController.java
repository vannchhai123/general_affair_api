package com.norton.backend.controllers.superadmin;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SuperAdminRoleController.BASE_URL)
public class SuperAdminRoleController {

  public static final String BASE_URL = "/api/v1/super-admin/roles";

  private final RoleService roleService;
  private final SecurityUtils securityUtils;

  @GetMapping
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', T(com.norton.backend.security.Permissions).ROLE_VIEW)")
  public ResponseEntity<PageResponse<RoleResponse>> getAllRoles(
      @RequestParam(required = false) String keyword,
      @PageableDefault(page = 0, size = 20, sort = "hierarchyLevel", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return ResponseEntity.ok(roleService.getAllRoles(keyword, pageable));
  }

  @GetMapping("/simple")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<RoleSimpleResponse>> getSimpleRoles() {
    return ResponseEntity.ok(roleService.getSimpleRoles());
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', T(com.norton.backend.security.Permissions).ROLE_VIEW)")
  public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
    return ResponseEntity.ok(roleService.getRoleById(id));
  }

  @PostMapping
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', T(com.norton.backend.security.Permissions).ROLE_CREATE)")
  public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    RoleResponse response = roleService.createRole(request, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', T(com.norton.backend.security.Permissions).ROLE_UPDATE)")
  public ResponseEntity<RoleResponse> updateRole(
      @PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    RoleResponse response = roleService.updateRole(id, request, currentUser);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', T(com.norton.backend.security.Permissions).ROLE_DELETE)")
  public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
    UserModel currentUser = securityUtils.getCurrentUser();
    roleService.deleteRole(id, currentUser);
    return ResponseEntity.ok(
        Map.of("success", true, "message", "Role deleted successfully", "id", id));
  }

  @PutMapping("/{id}/permissions")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', T(com.norton.backend.security.Permissions).ROLE_ASSIGN_PERMISSION)")
  public ResponseEntity<RoleResponse> syncRolePermissions(
      @PathVariable Long id, @Valid @RequestBody SyncRolePermissionsRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    RoleResponse response =
        roleService.syncRolePermissionsAndReturn(id, request.getPermissionIds(), currentUser);
    return ResponseEntity.ok(response);
  }
}
