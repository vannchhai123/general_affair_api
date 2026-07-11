package com.norton.backend.security;

import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    String processedUsername =
        username != null ? username.trim().toLowerCase(java.util.Locale.ROOT) : "";
    com.norton.backend.models.UserModel user =
        userRepository
            .findByUsername(processedUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

    // Initialize lazy fields into transient cache while session is open
    user.getAuthorities();

    return user;
  }
}
