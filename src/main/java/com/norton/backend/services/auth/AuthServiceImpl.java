package com.norton.backend.services.auth;

import com.norton.backend.dto.request.LoginRequest;
import com.norton.backend.dto.responses.AuthResponse;
import com.norton.backend.dto.responses.UserDto;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.mapper.UserMapper;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.security.CustomUserDetailsService;
import com.norton.backend.security.JwtService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final CustomUserDetailsService customUserDetailsService;
  private final JwtService jwtService;

  @Override
  public AuthResponse<UserDto> login(LoginRequest request) {

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    UserModel user = (UserModel) customUserDetailsService.loadUserByUsername(request.getUsername());
    String accessToken = jwtService.generateToken(Map.of(), user);
    String refreshToken = jwtService.generateRefreshToken(user);

    UserDto userDto = userMapper.toDto(user);

    return AuthResponse.<UserDto>builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .data(userDto)
        .build();
  }

  @Override
  public AuthResponse<UserDto> refreshToken(String refreshToken) {

    if (!jwtService.isRefreshTokenValid(refreshToken)) {
      throw new RuntimeException("Invalid refresh token");
    }

    String username = jwtService.extractUsername(refreshToken);
    UserModel user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String accessToken = jwtService.generateToken(Map.of(), user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    return AuthResponse.<UserDto>builder()
        .accessToken(accessToken)
        .refreshToken(newRefreshToken)
        .data(userMapper.toDto(user))
        .build();
  }

  @Override
  public MeResponse getMyProfile() {
    UserModel currentUser =
        (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    return userMapper.toMeResponse(currentUser);
  }
}
