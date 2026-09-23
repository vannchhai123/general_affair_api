package com.norton.backend.seeds;

import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
// @Profile("dev")
public class UserRoleDataLoading implements CommandLineRunner {

  private final UserRoleRepository roleRepository;
  private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

  @Override
  public void run(String... args) {
    try {
      jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN role_id DROP NOT NULL;");
    } catch (Exception ignored) {
      // Column may already be nullable or not exist
    }

    try {
      jdbcTemplate.execute(
          "INSERT INTO user_roles_mapping (user_id, role_id) "
              + "SELECT id, role_id FROM users "
              + "WHERE role_id IS NOT NULL "
              + "ON CONFLICT DO NOTHING;");
    } catch (Exception ignored) {
    }
    loadRole(
        "ROLE_SUPER_ADMIN",
        "អភិបាលជាន់ខ្ពស់",
        "Super Administrator",
        1,
        true,
        "Maximum authority to manage the entire platform");
    loadRole(
        "ROLE_ADMIN",
        "នាយកគ្រប់គ្រងប្រព័ន្ធ",
        "System Administrator",
        1,
        true,
        "Administrator with full access");
    loadRole(
        "ROLE_GOVERNOR",
        "អភិបាលរាជធានី-ខេត្ត",
        "Governor",
        2,
        false,
        "Governor with executive access");
    loadRole(
        "ROLE_DEPUTY_GOVERNOR", "អភិបាលរង", "Deputy Governor", 3, false, "Deputy Governor role");
    loadRole(
        "ROLE_HEAD_OFFICE",
        "នាយករដ្ឋបាល",
        "Head of Administration",
        4,
        true,
        "Head Office for daily operations");
    loadRole("ROLE_MANAGER", "ប្រធានការិយាល័យ", "Office Manager", 5, true, "Manager role");
    loadRole("ROLE_OFFICER", "មន្ត្រីរាជការ", "Civil Officer", 6, true, "Officer role");
  }

  private void loadRole(
      String code,
      String nameKm,
      String nameEn,
      int hierarchyLevel,
      boolean isSystem,
      String description) {

    UserRoleModel role =
        roleRepository
            .findByRoleName(code)
            .orElseGet(
                () ->
                    UserRoleModel.builder()
                        .code(code)
                        .roleName(code)
                        .nameKm(nameKm)
                        .nameEn(nameEn)
                        .hierarchyLevel(hierarchyLevel)
                        .isSystem(isSystem)
                        .description(description)
                        .build());

    role.setCode(code);
    role.setRoleName(code);
    role.setNameKm(nameKm);
    role.setNameEn(nameEn);
    role.setHierarchyLevel(hierarchyLevel);
    role.setIsSystem(isSystem);
    role.setDescription(description);

    roleRepository.save(role);
    System.out.println("✅ Seeded role: " + code + " (" + nameKm + ")");
  }
}
