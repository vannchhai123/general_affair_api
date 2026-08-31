package com.norton.backend.controllers.superadmin;

import com.norton.backend.dto.request.superadmin.AdminResetPasswordRequest;
import com.norton.backend.dto.request.superadmin.AssignUserRoleRequest;
import com.norton.backend.dto.request.superadmin.UpdateUserStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.superadmin.SuperAdminUserDetailResponse;
import com.norton.backend.models.UserModel;
import com.norton.backend.services.superadmin.SuperAdminUserService;
import com.norton.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping(SuperAdminUserController.BASE_URL)
public class SuperAdminUserController {

  public static final String BASE_URL = "/api/v1/super-admin/users";

  private final SuperAdminUserService superAdminUserService;
  private final SecurityUtils securityUtils;

  @GetMapping
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<PageResponse<SuperAdminUserDetailResponse>> getAllUsers(
      @RequestParam(required = false) String keyword,
      @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(superAdminUserService.getAllUsers(keyword, pageable));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<SuperAdminUserDetailResponse> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(superAdminUserService.getUserById(id));
  }

  @PutMapping("/{id}/status")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<SuperAdminUserDetailResponse> updateUserStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    return ResponseEntity.ok(superAdminUserService.updateUserStatus(id, request, currentUser));
  }

  @PutMapping(value = {"/{id}/role", "/{id}/roles"})
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<SuperAdminUserDetailResponse> assignUserRole(
      @PathVariable Long id, @Valid @RequestBody AssignUserRoleRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    return ResponseEntity.ok(superAdminUserService.assignUserRole(id, request, currentUser));
  }

  @PostMapping("/{id}/reset-password")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<Map<String, Object>> resetUserPassword(
      @PathVariable Long id, @Valid @RequestBody AdminResetPasswordRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    superAdminUserService.resetUserPassword(id, request, currentUser);
    return ResponseEntity.ok(
        Map.of("success", true, "message", "User password reset successfully", "userId", id));
  }
}
