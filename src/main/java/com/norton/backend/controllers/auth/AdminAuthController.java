package com.norton.backend.controllers.auth;

import com.norton.backend.dto.responses.auth.AdminMeResponse;
import com.norton.backend.models.UserModel;
import com.norton.backend.services.auth.AuthService;
import com.norton.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping({AdminAuthController.BASE_URL, "/admin/auth"})
public class AdminAuthController {

  public static final String BASE_URL = "/api/v1/admin/auth";

  private final AuthService authService;
  private final SecurityUtils securityUtils;

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AdminMeResponse> getAdminMe() {
    UserModel currentUser = securityUtils.getCurrentUser();
    return ResponseEntity.ok(authService.getAdminMeProfile(currentUser));
  }
}
