package com.norton.backend.controllers.role;

import com.norton.backend.dto.responses.role.AdminRoleDto;
import com.norton.backend.services.role.RoleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping({AdminRoleController.BASE_URL, "/admin/roles"})
public class AdminRoleController {

  public static final String BASE_URL = "/api/v1/admin/roles";

  private final RoleService roleService;

  @GetMapping
  @PreAuthorize(
      "isAuthenticated() or hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', T(com.norton.backend.security.Permissions).ROLE_VIEW)")
  public ResponseEntity<List<AdminRoleDto>> getAdminRoles() {
    return ResponseEntity.ok(roleService.getAdminRoles());
  }
}
