package com.norton.backend.controllers.superadmin;

import com.norton.backend.dto.request.LoginRequest;
import com.norton.backend.dto.request.RefreshRequest;
import com.norton.backend.dto.responses.AuthResponse;
import com.norton.backend.dto.responses.UserDto;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.models.UserModel;
import com.norton.backend.services.superadmin.SuperAdminAuthService;
import com.norton.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SuperAdminAuthController.BASE_URL)
public class SuperAdminAuthController {

  public static final String BASE_URL = "/api/v1/super-admin/auth";

  private final SuperAdminAuthService superAdminAuthService;
  private final SecurityUtils securityUtils;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse<UserDto>> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(superAdminAuthService.superAdminLogin(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse<UserDto>> refreshToken(@RequestBody RefreshRequest request) {
    return ResponseEntity.ok(
        superAdminAuthService.superAdminRefreshToken(request.getRefreshToken()));
  }

  @GetMapping("/me")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<MeResponse> getProfile() {
    UserModel currentUser = securityUtils.getCurrentUser();
    return ResponseEntity.ok(superAdminAuthService.getSuperAdminProfile(currentUser));
  }
}
