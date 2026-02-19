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
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
  }
}
