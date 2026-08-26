package com.norton.backend.services.superadmin;

import com.norton.backend.dto.request.LoginRequest;
import com.norton.backend.dto.responses.AuthResponse;
import com.norton.backend.dto.responses.UserDto;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.models.UserModel;

public interface SuperAdminAuthService {

  AuthResponse<UserDto> superAdminLogin(LoginRequest request);

  AuthResponse<UserDto> superAdminRefreshToken(String refreshToken);

  MeResponse getSuperAdminProfile(UserModel currentUser);
}
