package com.norton.backend.services.superadmin;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SuperAdminAuthServiceImpl implements SuperAdminAuthService {

  private final AuthenticationManager authenticationManager;
  private final CustomUserDetailsService customUserDetailsService;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final UserRepository userRepository;

  @Override
  public AuthResponse<UserDto> superAdminLogin(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    UserModel user = (UserModel) customUserDetailsService.loadUserByUsername(request.getUsername());

    validateSuperAdminAccess(user);

    String accessToken = jwtService.generateToken(Map.of("portal", "super-admin"), user);
    String refreshToken = jwtService.generateRefreshToken(user);

    UserDto userDto = userMapper.toDto(user);

    log.info("Super Admin logged in successfully: username={}", user.getUsername());

    return AuthResponse.<UserDto>builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .data(userDto)
        .build();
  }

  @Override
  public AuthResponse<UserDto> superAdminRefreshToken(String refreshToken) {
    if (!jwtService.isRefreshTokenValid(refreshToken)) {
      throw new RuntimeException("Invalid refresh token");
    }

    String username = jwtService.extractUsername(refreshToken);
    UserModel user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    validateSuperAdminAccess(user);

    String accessToken = jwtService.generateToken(Map.of("portal", "super-admin"), user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    return AuthResponse.<UserDto>builder()
        .accessToken(accessToken)
        .refreshToken(newRefreshToken)
        .data(userMapper.toDto(user))
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public MeResponse getSuperAdminProfile(UserModel currentUser) {
    return userMapper.toMeResponse(currentUser);
  }

  private void validateSuperAdminAccess(UserModel user) {
    if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
      throw new AccessDeniedException(
          "Access denied: Only Super Administrators are allowed to log in.");
    }

    boolean hasSuperAdminRole =
        user.getRoles().stream()
            .anyMatch(
                r -> {
                  String code = r.getCode() != null ? r.getCode() : r.getRoleName();
                  Integer hierarchyLevel = r.getHierarchyLevel();
                  return "ROLE_ADMIN".equalsIgnoreCase(code)
                      || "ROLE_SUPER_ADMIN".equalsIgnoreCase(code)
                      || (hierarchyLevel != null && hierarchyLevel == 1);
                });

    if (!hasSuperAdminRole) {
      log.warn(
          "Denied Super Admin portal login attempt for user: username={}, roles={}",
          user.getUsername(),
          user.getRoles());
      throw new AccessDeniedException(
          "Access denied: Super Admin Portal requires Hierarchy Level 1 (System Administrator) privileges.");
    }
  }
}
