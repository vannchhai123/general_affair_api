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

    PermissionModel attendanceView =
        createPermission("ATTENDANCE_VIEW", "View attendance", "Attendance");
    PermissionModel attendanceCreate =
        createPermission("ATTENDANCE_CREATE", "Create attendance", "Attendance");
    PermissionModel attendanceUpdate =
        createPermission("ATTENDANCE_UPDATE", "Update attendance", "Attendance");
    PermissionModel attendanceExport =
        createPermission("ATTENDANCE_EXPORT", "Export attendance", "Attendance");
    PermissionModel attendanceImport =
        createPermission("ATTENDANCE_IMPORT", "Import attendance", "Attendance");
    PermissionModel attendanceScan =
        createPermission("ATTENDANCE_SCAN", "Submit attendance scan", "Attendance");

    PermissionModel shiftView = createPermission("SHIFT_VIEW", "View shifts", "Shift");
    PermissionModel shiftCreate = createPermission("SHIFT_CREATE", "Create shifts", "Shift");
    PermissionModel shiftUpdate = createPermission("SHIFT_UPDATE", "Update shifts", "Shift");
    PermissionModel shiftDelete = createPermission("SHIFT_DELETE", "Delete shifts", "Shift");
    PermissionModel shiftAssign =
        createPermission("SHIFT_ASSIGN", "Assign shift templates", "ShiftAssignment");

    PermissionModel organizationView =
        createPermission("ORGANIZATION_VIEW", "View departments and positions", "Organization");
    PermissionModel organizationCreate =
        createPermission("ORGANIZATION_CREATE", "Create departments and positions", "Organization");
    PermissionModel organizationUpdate =
        createPermission("ORGANIZATION_UPDATE", "Update departments and positions", "Organization");
    PermissionModel organizationDelete =
        createPermission("ORGANIZATION_DELETE", "Delete departments and positions", "Organization");

    PermissionModel qrView = createPermission("QR_SESSION_VIEW", "View QR sessions", "QrSession");
    PermissionModel qrCreate =
        createPermission("QR_SESSION_CREATE", "Create QR sessions", "QrSession");
    PermissionModel qrUpdate =
        createPermission("QR_SESSION_UPDATE", "Update QR sessions", "QrSession");
    PermissionModel qrEnd = createPermission("QR_SESSION_END", "End QR sessions", "QrSession");
    PermissionModel qrCheckin =
        createPermission("QR_SESSION_CHECKIN", "Create QR check-ins", "QrSession");

    PermissionModel dashboardView =
        createPermission("DASHBOARD_VIEW", "View dashboard", "Dashboard");

    Set<PermissionModel> superAdminPermissions =
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
            officerViewPermission,
            attendanceView,
            attendanceCreate,
            attendanceUpdate,
            attendanceExport,
            attendanceImport,
            attendanceScan,
            shiftView,
            shiftCreate,
            shiftUpdate,
            shiftDelete,
            shiftAssign,
            organizationView,
            organizationCreate,
            organizationUpdate,
            organizationDelete,
            qrView,
            qrCreate,
            qrUpdate,
            qrEnd,
            qrCheckin,
            dashboardView);

    createOrUpdateRole(
        "ROLE_SUPER_ADMIN", "Super Administrator with full system control", superAdminPermissions);

    createOrUpdateRole(
        "ROLE_ADMIN",
        "Administrator for daily operations",
        Set.of(
            officerView,
            officerCreate,
            officerUpdate,
            officerDelete,
            officerViewPermission,
            attendanceView,
            attendanceCreate,
            attendanceUpdate,
            attendanceExport,
            attendanceImport,
            attendanceScan,
            organizationView,
            organizationCreate,
            organizationUpdate,
            organizationDelete,
            qrView,
            qrCreate,
            qrUpdate,
            qrEnd,
            qrCheckin,
            dashboardView));

    createOrUpdateRole(
        "ROLE_MANAGER",
        "Manager with approval and reporting access",
        Set.of(
            officerView,
            attendanceView,
            attendanceUpdate,
            attendanceExport,
            attendanceScan,
            organizationView,
            qrView,
            qrCheckin,
            dashboardView));

    createOrUpdateRole(
        "ROLE_OFFICER",
        "Officer with self-service access",
        Set.of(attendanceView, attendanceScan, qrView, qrCheckin, dashboardView));
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
