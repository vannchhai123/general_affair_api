package com.norton.backend.seeds;

import com.norton.backend.enums.UserStatus;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
//@Profile("dev")
public class UserDataLoading implements CommandLineRunner {

  private final UserRepository userRepository;
  private final UserRoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {

    UserRoleModel adminRole = createRoleIfNotExists("ROLE_ADMIN", "System Administrator");
    UserRoleModel userRole = createRoleIfNotExists("ROLE_USER", "Default User");

    createUserIfNotExists(
        "admin", "admin@gmail.com", "Admin User", "admin123", adminRole, UserStatus.ACTIVE);

    createUserIfNotExists(
        "user", "user@gmail.com", "John Doe", "user123", userRole, UserStatus.ACTIVE);

    createUserIfNotExists(
        "banned", "banned@gmail.com", "Banned User", "banned123", userRole, UserStatus.BANNED);

    System.out.println("✅ Seed data loaded successfully!");
  }

  private UserRoleModel createRoleIfNotExists(String roleName, String description) {
    return roleRepository
        .findByRoleName(roleName)
        .orElseGet(
            () ->
                roleRepository.save(
                    UserRoleModel.builder().roleName(roleName).description(description).build()));
  }

  private void createUserIfNotExists(
      String username,
      String email,
      String fullName,
      String rawPassword,
      UserRoleModel role,
      UserStatus status) {

    if (!userRepository.existsByUsername(username)) {

      UserModel user =
          UserModel.builder()
              .username(username)
              .email(email)
              .fullName(fullName)
              .passwordHash(passwordEncoder.encode(rawPassword))
              .role(role)
              .userStatus(status)
              .build();

      userRepository.save(user);
    }
  }
}
