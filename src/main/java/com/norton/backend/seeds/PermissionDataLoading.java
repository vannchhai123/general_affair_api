package com.norton.backend.seeds;

import com.norton.backend.models.PermissionModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.PermissionRepository;
import com.norton.backend.repositories.UserRoleRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
// @Profile("dev")
@Transactional
public class PermissionDataLoading implements CommandLineRunner {

  private final PermissionRepository permissionRepository;
  private final UserRoleRepository roleRepository;

  @Override
  public void run(String... args) {

    PermissionModel permissionView =
        createPermission("PERMISSION_VIEW", "View permissions", "Permission");
    PermissionModel permissionCreate =
        createPermission("PERMISSION_CREATE", "Create permissions", "Permission");
    PermissionModel permissionUpdate =
        createPermission("PERMISSION_UPDATE", "Update permissions", "Permission");
    PermissionModel permissionDelete =
        createPermission("PERMISSION_DELETE", "Delete permissions", "Permission");

    PermissionModel roleAssign =
        createPermission("ROLE_ASSIGN_PERMISSION", "Assign permissions to role", "Role");
    PermissionModel roleRemove =
        createPermission("ROLE_REMOVE_PERMISSION", "Remove permissions from role", "Role");
    PermissionModel roleView = createPermission("ROLE_VIEW", "View roles", "Role");

    PermissionModel officerView = createPermission("OFFICER_VIEW", "View officers", "Officer");
    PermissionModel officerCreate = createPermission("OFFICER_CREATE", "Create officer", "Officer");
    PermissionModel officerUpdate = createPermission("OFFICER_UPDATE", "Update officer", "Officer");
    PermissionModel officerDelete = createPermission("OFFICER_DELETE", "Delete officer", "Officer");

    PermissionModel officerAssignPermission =
        createPermission(
            "OFFICER_ASSIGN_PERMISSION", "Assign permission to officer", "OfficerPermission");
    PermissionModel officerRemovePermission =
        createPermission(
            "OFFICER_REMOVE_PERMISSION", "Remove permission from officer", "OfficerPermission");
    PermissionModel officerViewPermission =
        createPermission(
            "OFFICER_VIEW_PERMISSION", "View officer permissions", "OfficerPermission");

    // =========================
    // ROLES
    // =========================

    // 👑 ADMIN (FULL ACCESS)
    createOrUpdateRole(
        "ROLE_ADMIN",
        "System Administrator",
        Set.of(
            permissionView,
            permissionCreate,
            permissionUpdate,
            permissionDelete,
            roleAssign,
            roleRemove,
            roleView,
            officerView,
            officerCreate,
            officerUpdate,
            officerDelete,
            officerAssignPermission,
            officerRemovePermission,
            officerViewPermission));

    createOrUpdateRole(
        "ROLE_MANAGER",
        "Manager Role",
        Set.of(officerView, officerCreate, officerUpdate, officerViewPermission));

    createOrUpdateRole("ROLE_OFFICER", "Officer Role", Set.of(officerView, officerViewPermission));
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

    role.getPermissions().clear();
    role.getPermissions().addAll(permissions);

    roleRepository.save(role);
  }
}
