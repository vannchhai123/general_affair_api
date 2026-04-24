package com.norton.backend.services.auth;

import com.norton.backend.dto.request.ChangePasswordRequest;
import com.norton.backend.dto.request.ForgotPasswordVerifyEmailRequest;
import com.norton.backend.dto.request.LoginRequest;
import com.norton.backend.dto.responses.AuthResponse;
import com.norton.backend.dto.responses.UserDto;
import com.norton.backend.dto.responses.officers.MeResponse;
import java.util.Map;

public interface AuthService {
  AuthResponse<UserDto> login(LoginRequest request);

  AuthResponse<UserDto> refreshToken(String refreshToken);

  MeResponse getMyProfile();

  Map<String, String> changePassword(ChangePasswordRequest request);

  Map<String, String> forgotPasswordVerifyEmail(ForgotPasswordVerifyEmailRequest request);
}
