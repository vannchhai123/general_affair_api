package com.norton.backend.services.auth;

import com.norton.backend.dto.request.LoginRequest;
import com.norton.backend.dto.responses.AuthResponse;
import com.norton.backend.dto.responses.UserDto;

public interface AuthService {
  AuthResponse<UserDto> login(LoginRequest request);
}
