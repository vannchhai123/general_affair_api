package com.norton.backend.services.superadmin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.norton.backend.dto.request.superadmin.SyncUserAccessRequest;
import com.norton.backend.dto.responses.superadmin.UserAccessResponse;
import com.norton.backend.enums.UserStatus;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.models.*;
import com.norton.backend.repositories.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class SuperAdminUserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private OfficerRepository officerRepository;
  @Mock private PermissionRepository permissionRepository;
  @Mock private AuditLogRepository auditLogRepository;

  @InjectMocks private SuperAdminUserServiceImpl userService;

  private UserModel superAdminUser;
  private UserModel normalUser;
  private OfficerModel officer;
  private UserRoleModel officerRole;
  private UserRoleModel chiefRole;
  private PermissionModel permDashboard;
  private PermissionModel permExport;
  private PermissionModel permInvitation;

  @BeforeEach
  void setUp() {
    UserRoleModel superAdminRole =
        UserRoleModel.builder().code("ROLE_SUPER_ADMIN").hierarchyLevel(1).build();
    superAdminRole.setId(1L);
    superAdminUser =
        UserModel.builder()
            .fullName("Admin Super")
            .username("admin")
            .email("admin@domain.gov.kh")
            .role(superAdminRole)
            .build();
    superAdminUser.setId(1L);

    permDashboard = PermissionModel.builder().permissionName("DASHBOARD_VIEW").build();
    permDashboard.setId(10L);

    permExport = PermissionModel.builder().permissionName("ATTENDANCE_EXPORT").build();
    permExport.setId(11L);

    permInvitation = PermissionModel.builder().permissionName("INVITATION_CREATE").build();
    permInvitation.setId(12L);

    officerRole =
        UserRoleModel.builder()
            .code("ROLE_OFFICER")
            .nameEn("Officer")
            .nameKm("មន្ត្រី")
            .hierarchyLevel(10)
            .permissions(new HashSet<>(Set.of(permDashboard)))
            .build();
    officerRole.setId(3L);

    chiefRole =
        UserRoleModel.builder()
            .code("ROLE_OFFICE_CHIEF")
            .nameEn("Office Chief")
            .nameKm("ប្រធានការិយាល័យ")
            .hierarchyLevel(5)
            .permissions(new HashSet<>())
            .build();
    chiefRole.setId(5L);

    normalUser =
        UserModel.builder()
            .fullName("Sokha Mean")
            .username("sokha")
            .email("sokha@domain.gov.kh")
            .userStatus(UserStatus.ACTIVE)
            .roles(new HashSet<>(Set.of(officerRole)))
            .build();
    normalUser.setId(5L);

    officer =
        OfficerModel.builder()
            .officerCode("OFF-005")
            .firstNameEn("Sokha")
            .lastNameEn("Mean")
            .firstNameKh("សុខា")
            .lastNameKh("មាន")
            .user(normalUser)
            .officerPermissions(new ArrayList<>())
            .build();
    officer.setId(12L);
    normalUser.setOfficer(officer);
  }

  @Test
  void testSyncUserAccess_SuccessWithRolesAndDirectPermissions() {
    SyncUserAccessRequest request =
        SyncUserAccessRequest.builder()
            .roleIds(List.of(3L, 5L))
            .directPermissions(List.of("ATTENDANCE_EXPORT", "INVITATION_CREATE"))
            .reason("Promoted to event coordinator with temporary export duty")
            .build();

    when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
    when(userRoleRepository.findAllById(List.of(3L, 5L)))
        .thenReturn(List.of(officerRole, chiefRole));
    when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArgument(0));
    when(permissionRepository.findAllByPermissionNameIn(
            List.of("ATTENDANCE_EXPORT", "INVITATION_CREATE")))
        .thenReturn(List.of(permExport, permInvitation));
    when(officerRepository.save(any(OfficerModel.class))).thenAnswer(i -> i.getArgument(0));

    UserAccessResponse response = userService.syncUserAccess(5L, request, superAdminUser);

    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("Officer roles and permissions updated successfully", response.getMessage());

    UserAccessResponse.UserAccessData data = response.getData();
    assertNotNull(data);
    assertEquals(12L, data.getOfficerId());
    assertEquals(5L, data.getUserId());
    assertEquals(2, data.getAssignedRoles().size());
    assertEquals(2, data.getDirectPermissions().size());
    assertTrue(data.getDirectPermissions().contains("ATTENDANCE_EXPORT"));
    assertTrue(data.getDirectPermissions().contains("INVITATION_CREATE"));

    // Effective permissions: DASHBOARD_VIEW (from role) + ATTENDANCE_EXPORT + INVITATION_CREATE
    // (direct)
    assertEquals(3, data.getEffectivePermissions().size());
    assertTrue(data.getEffectivePermissions().contains("DASHBOARD_VIEW"));
    assertTrue(data.getEffectivePermissions().contains("ATTENDANCE_EXPORT"));
    assertTrue(data.getEffectivePermissions().contains("INVITATION_CREATE"));

    verify(auditLogRepository, times(1)).save(any(AuditLogModel.class));
  }

  @Test
  void testSyncUserAccess_RoleNotFound_ThrowsBadRequest() {
    SyncUserAccessRequest request = SyncUserAccessRequest.builder().roleIds(List.of(999L)).build();

    when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
    when(userRoleRepository.findAllById(List.of(999L))).thenReturn(List.of());

    assertThrows(
        BadRequestException.class, () -> userService.syncUserAccess(5L, request, superAdminUser));
  }

  @Test
  void testSyncUserAccess_PermissionNotFound_ThrowsBadRequest() {
    SyncUserAccessRequest request =
        SyncUserAccessRequest.builder().directPermissions(List.of("UNKNOWN_PERMISSION")).build();

    when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
    when(permissionRepository.findAllByPermissionNameIn(List.of("UNKNOWN_PERMISSION")))
        .thenReturn(List.of());

    assertThrows(
        BadRequestException.class, () -> userService.syncUserAccess(5L, request, superAdminUser));
  }

  @Test
  void testSyncOfficerAccess_Success() {
    SyncUserAccessRequest request =
        SyncUserAccessRequest.builder()
            .roleIds(List.of(3L))
            .directPermissions(List.of("ATTENDANCE_EXPORT"))
            .build();

    when(officerRepository.findById(12L)).thenReturn(Optional.of(officer));
    when(userRoleRepository.findAllById(List.of(3L))).thenReturn(List.of(officerRole));
    when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArgument(0));
    when(permissionRepository.findAllByPermissionNameIn(List.of("ATTENDANCE_EXPORT")))
        .thenReturn(List.of(permExport));
    when(officerRepository.save(any(OfficerModel.class))).thenAnswer(i -> i.getArgument(0));

    UserAccessResponse response = userService.syncOfficerAccess(12L, request, superAdminUser);

    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals(12L, response.getData().getOfficerId());
    assertEquals(5L, response.getData().getUserId());
    assertEquals(1, response.getData().getDirectPermissions().size());
  }

  @Test
  void testSyncUserAccess_PrivilegeEscalation_ThrowsAccessDenied() {
    UserRoleModel lowLevelRole =
        UserRoleModel.builder().code("ROLE_OFFICER").hierarchyLevel(10).build();
    lowLevelRole.setId(99L);
    UserModel lowerAdmin = UserModel.builder().role(lowLevelRole).build();
    lowerAdmin.setId(99L);

    SyncUserAccessRequest request =
        SyncUserAccessRequest.builder()
            .roleIds(List.of(5L)) // Chief role has hierarchy 5, which is higher privilege than 10
            .build();

    when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
    when(userRoleRepository.findAllById(List.of(5L))).thenReturn(List.of(chiefRole));

    assertThrows(
        AccessDeniedException.class, () -> userService.syncUserAccess(5L, request, lowerAdmin));
  }

  @Test
  void testGetUserAccessDetails_Success() {
    OfficerPermission op =
        OfficerPermission.builder().officer(officer).permission(permExport).build();
    officer.getOfficerPermissions().add(op);

    when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));

    com.norton.backend.dto.responses.superadmin.UserAccessDetailResponse response =
        userService.getUserAccessDetails(5L);

    assertNotNull(response);
    assertEquals(12L, response.getOfficerId());
    assertEquals(5L, response.getUserId());
    assertEquals("Sokha Mean", response.getFullName());
    assertEquals(1, response.getAssignedRoles().size());
    assertEquals("ROLE_OFFICER", response.getAssignedRoles().get(0).getCode());
    assertEquals(1, response.getAssignedRoles().get(0).getPermissions().size());
    assertTrue(response.getAssignedRoles().get(0).getPermissions().contains("DASHBOARD_VIEW"));
    assertEquals(1, response.getDirectPermissions().size());
    assertTrue(response.getDirectPermissions().contains("ATTENDANCE_EXPORT"));
    assertEquals(2, response.getEffectivePermissions().size());
    assertTrue(response.getEffectivePermissions().contains("DASHBOARD_VIEW"));
    assertTrue(response.getEffectivePermissions().contains("ATTENDANCE_EXPORT"));
  }

  @Test
  void testGetOfficerAccessDetails_Success() {
    when(officerRepository.findById(12L)).thenReturn(Optional.of(officer));

    com.norton.backend.dto.responses.superadmin.UserAccessDetailResponse response =
        userService.getOfficerAccessDetails(12L);

    assertNotNull(response);
    assertEquals(12L, response.getOfficerId());
    assertEquals(5L, response.getUserId());
  }
}
