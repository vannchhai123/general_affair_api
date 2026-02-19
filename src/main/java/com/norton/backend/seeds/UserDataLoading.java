package com.norton.backend.seeds;

import com.norton.backend.enums.UserStatus;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDataLoading implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRoleRepository roleRepository;

  @Override
  public void run(String... args) throws Exception {
    if (roleRepository.count() == 0) {
      UserRoleModel adminRole = UserRoleModel.builder().roleName("ADMIN").build();
      UserRoleModel userRole = UserRoleModel.builder().roleName("USER").build();
      roleRepository.saveAll(List.of(adminRole, userRole));
    }

    UserRoleModel adminRole = roleRepository.findByRoleName("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
    UserRoleModel userRole = roleRepository.findByRoleName("USER")
            .orElseThrow(() -> new RuntimeException("USER role not found"));

    if (userRepository.count() == 0) {
      UserModel admin = UserModel.builder()
              .fullName("Admin User")
              .email("admin@gmail.com")
              .passwordHash(passwordEncoder.encode("admin123"))
              .role(adminRole)
              .userStatus(UserStatus.ACTIVE)
              .build();

      UserModel normalUser = UserModel.builder()
              .fullName("John Doe")
              .email("user@gmail.com")
              .passwordHash(passwordEncoder.encode("user123"))
              .role(userRole)
              .userStatus(UserStatus.ACTIVE)
              .build();

      UserModel bannedUser = UserModel.builder()
              .fullName("Banned User")
              .email("banned@gmail.com")
              .passwordHash(passwordEncoder.encode("banned123"))
              .role(userRole)
              .userStatus(UserStatus.BANNED)
              .build();

      userRepository.saveAll(List.of(admin, normalUser, bannedUser));
    }

    System.out.println("✅ Seed data loaded successfully!");
  }
}
