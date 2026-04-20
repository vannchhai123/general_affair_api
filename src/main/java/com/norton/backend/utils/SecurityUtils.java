package com.norton.backend.utils;

import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
  private final UserRepository userRepository;

  public String getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    return authentication.getName();
  }

  public Long getCurrentUserId() {
    String username = getCurrentUsername();

    UserModel user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

    return user.getId();
  }
}
