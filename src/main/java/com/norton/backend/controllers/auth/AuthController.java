package com.norton.backend.controllers.auth;

import com.norton.backend.dto.request.ChangePasswordRequest;
import com.norton.backend.dto.request.ForgotPasswordResetRequest;
import com.norton.backend.dto.request.ForgotPasswordVerifyEmailRequest;
import com.norton.backend.dto.request.ForgotPasswordVerifyOtpRequest;
import com.norton.backend.dto.request.LoginRequest;
import com.norton.backend.dto.request.RefreshRequest;
import com.norton.backend.dto.responses.AuthResponse;
import com.norton.backend.dto.responses.UserDto;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.services.auth.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(AuthController.BASE_URL)
public class AuthController {

  public static final String BASE_URL = "/api/v1/auth";

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse<UserDto>> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse<UserDto>> refreshToken(@RequestBody RefreshRequest request) {

    AuthResponse<UserDto> response = authService.refreshToken(request.getRefreshToken());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<MeResponse> me() {
    return ResponseEntity.ok(authService.getMyProfile());
  }

  @PostMapping("/change-password")
  public ResponseEntity<Map<String, String>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request) {
    return ResponseEntity.ok(authService.changePassword(request));
  }

  @PostMapping("/forgot-password/verify-email")
  public ResponseEntity<Map<String, String>> forgotPasswordVerifyEmail(
      @Valid @RequestBody ForgotPasswordVerifyEmailRequest request) {
    return ResponseEntity.ok(authService.forgotPasswordVerifyEmail(request));
  }

  @PostMapping("/forgot-password/verify-otp")
  public ResponseEntity<Map<String, String>> forgotPasswordVerifyOtp(
      @Valid @RequestBody ForgotPasswordVerifyOtpRequest request) {
    return ResponseEntity.ok(authService.forgotPasswordVerifyOtp(request));
  }

  @PostMapping({"/forgot-password/reset", "/forgot-password/reset-password"})
  public ResponseEntity<Map<String, String>> forgotPasswordReset(
      @Valid @RequestBody ForgotPasswordResetRequest request) {
    return ResponseEntity.ok(authService.forgotPasswordReset(request));
  }
}
