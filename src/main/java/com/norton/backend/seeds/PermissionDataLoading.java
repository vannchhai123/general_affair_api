package com.norton.backend.seeds;

import com.norton.backend.models.PermissionModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.PermissionRepository;
import com.norton.backend.repositories.UserRoleRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
@Profile("dev")
public class PermissionDataLoading implements CommandLineRunner {

  private final PermissionRepository permissionRepository;
  private final UserRoleRepository roleRepository;

  @Override
  public void run(String... args) {

    PermissionModel officerView =
        createPermission("officer.view", "View officer records", "Officers");
    PermissionModel officerCreate =
        createPermission("officer.create", "Create new officer records", "Officers");
    PermissionModel officerEdit =
        createPermission("officer.edit", "Edit officer records", "Officers");
    PermissionModel officerDelete =
        createPermission("officer.delete", "Delete officer records", "Officers");

    PermissionModel manageRoles =
        createPermission("MANAGE_ROLES", "Manage system roles and permissions", "System");

    createOrUpdateRole(
        "ROLE_ADMIN",
        "System Administrator",
        Set.of(officerView, officerCreate, officerEdit, officerDelete, manageRoles));

    createOrUpdateRole(
        "ROLE_MANAGER", "Manager Role", Set.of(officerView, officerCreate, officerEdit));

    createOrUpdateRole("ROLE_OFFICER", "Officer Role", Set.of(officerView));
  }

  private PermissionModel createPermission(String name, String description, String category) {
    return permissionRepository
        .findByPermissionName(name)
        .orElseGet(
            () ->
                permissionRepository.save(
                    PermissionModel.builder()
                        .permissionName(name)
                        .description(description)
                        .category(category)
                        .build()));
  }

  /** Create or update role with permissions */
  private void createOrUpdateRole(
      String roleName, String description, Set<PermissionModel> permissions) {

    UserRoleModel role =
        roleRepository
            .findByRoleName(roleName)
            .orElseGet(
                () ->
                    roleRepository.save(
                        UserRoleModel.builder()
                            .roleName(roleName)
                            .description(description)
                            .permissions(new HashSet<>())
                            .build()));

    // Reset and set permissions
    role.getPermissions().clear();
    role.getPermissions().addAll(permissions);

    roleRepository.save(role);
  }
}
