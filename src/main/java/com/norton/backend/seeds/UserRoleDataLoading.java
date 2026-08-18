package com.norton.backend.seeds;

import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
@Profile("dev")
public class UserRoleDataLoading implements CommandLineRunner {

  private final UserRoleRepository roleRepository;

  @Override
  public void run(String... args) {

    loadRole("ROLE_ADMIN", "Administrator with full access");
    loadRole("ROLE_HEAD_OFFICE", "Head Office for daily operations");
    loadRole("ROLE_MANAGER", "Manager role");
    loadRole("ROLE_OFFICER", "Officer role");
  }
  private void loadRole(String roleName, String description) {

    if (!roleRepository.existsByRoleName(roleName)) {

      UserRoleModel role =
          UserRoleModel.builder().roleName(roleName).description(description).build();

      roleRepository.save(role);
      System.out.println("✅ Seeded role: " + roleName);
    }
  }
}
