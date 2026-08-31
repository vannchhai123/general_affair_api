package com.norton.backend.controllers.superadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.request.superadmin.AdminResetPasswordRequest;
import com.norton.backend.dto.request.superadmin.AssignUserRoleRequest;
import com.norton.backend.dto.request.superadmin.UpdateUserStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.dto.responses.superadmin.SuperAdminUserDetailResponse;
import com.norton.backend.enums.UserStatus;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.services.superadmin.SuperAdminUserService;
import com.norton.backend.utils.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SuperAdminUserControllerTest {

  @Mock private SuperAdminUserService superAdminUserService;
  @Mock private SecurityUtils securityUtils;

  @InjectMocks private SuperAdminUserController controller;

  private UserModel mockAdminUser;

  @BeforeEach
  void setUp() {
    UserRoleModel role =
        UserRoleModel.builder()
            .code("ROLE_SUPER_ADMIN")
            .nameEn("Super Admin")
            .hierarchyLevel(1)
            .build();
    mockAdminUser =
        UserModel.builder()
            .fullName("Kosol Chan")
            .username("superadmin")
            .email("superadmin@domain.gov.kh")
            .userStatus(UserStatus.ACTIVE)
            .role(role)
            .build();
    mockAdminUser.setId(1L);
  }

  @Test
  void testGetAllUsers_ReturnsPaginatedUsers() {
    SuperAdminUserDetailResponse userDto =
        SuperAdminUserDetailResponse.builder()
            .id(1L)
            .uuid("usr-001-superadmin")
            .username("superadmin")
            .email("superadmin@domain.gov.kh")
            .fullName("Kosol Chan")
            .userStatus(UserStatus.ACTIVE)
            .imageUrl(null)
            .role(
                RoleSimpleResponse.builder()
                    .id(1L)
                    .code("ROLE_SUPER_ADMIN")
                    .nameKm("អភិបាលជាន់ខ្ពស់")
                    .nameEn("Super Admin")
                    .hierarchyLevel(100)
                    .build())
            .officerId(101L)
            .officerCode("OFF-001")
            .departmentName("Cabinet of Provincial Administration")
            .positionName("Super Administrator")
            .permissions(List.of("ALL_PERMISSIONS"))
            .createdAt(Instant.parse("2026-08-31T08:00:00Z"))
            .updatedAt(Instant.parse("2026-08-31T08:00:00Z"))
            .build();

    PageResponse<SuperAdminUserDetailResponse> pageResponse =
        PageResponse.<SuperAdminUserDetailResponse>builder()
            .content(List.of(userDto))
            .totalElements(1)
            .totalPages(1)
            .page(0)
            .size(20)
            .first(true)
            .last(true)
            .empty(false)
            .build();

    Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
    when(superAdminUserService.getAllUsers(eq("superadmin"), any(Pageable.class)))
        .thenReturn(pageResponse);

    ResponseEntity<PageResponse<SuperAdminUserDetailResponse>> response =
        controller.getAllUsers("superadmin", pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getContent().size());
    assertEquals(1, response.getBody().getTotalElements());
    assertEquals(0, response.getBody().getPage());
    assertEquals(20, response.getBody().getSize());
    assertTrue(response.getBody().isFirst());
    assertTrue(response.getBody().isLast());
    assertFalse(response.getBody().isEmpty());

    SuperAdminUserDetailResponse firstUser = response.getBody().getContent().get(0);
    assertEquals("superadmin", firstUser.getUsername());
    assertEquals("OFF-001", firstUser.getOfficerCode());
    assertEquals("Cabinet of Provincial Administration", firstUser.getDepartmentName());
    assertEquals("ROLE_SUPER_ADMIN", firstUser.getRole().getCode());
  }

  @Test
  void testGetUserById_Success() {
    SuperAdminUserDetailResponse userDto =
        SuperAdminUserDetailResponse.builder()
            .id(1L)
            .username("superadmin")
            .fullName("Kosol Chan")
            .build();

    when(superAdminUserService.getUserById(1L)).thenReturn(userDto);

    ResponseEntity<SuperAdminUserDetailResponse> response = controller.getUserById(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("superadmin", response.getBody().getUsername());
  }

  @Test
  void testUpdateUserStatus_Success() {
    SuperAdminUserDetailResponse userDto =
        SuperAdminUserDetailResponse.builder()
            .id(1L)
            .username("superadmin")
            .userStatus(UserStatus.BANNED)
            .build();

    UpdateUserStatusRequest request =
        UpdateUserStatusRequest.builder().status(UserStatus.BANNED).build();

    when(securityUtils.getCurrentUser()).thenReturn(mockAdminUser);
    when(superAdminUserService.updateUserStatus(
            eq(1L), any(UpdateUserStatusRequest.class), eq(mockAdminUser)))
        .thenReturn(userDto);

    ResponseEntity<SuperAdminUserDetailResponse> response =
        controller.updateUserStatus(1L, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(UserStatus.BANNED, response.getBody().getUserStatus());
  }

  @Test
  void testAssignUserRole_Success() {
    SuperAdminUserDetailResponse userDto =
        SuperAdminUserDetailResponse.builder()
            .id(1L)
            .username("superadmin")
            .role(RoleSimpleResponse.builder().id(2L).code("ROLE_GOVERNOR").build())
            .build();

    AssignUserRoleRequest request = AssignUserRoleRequest.builder().roleId(2L).build();

    when(securityUtils.getCurrentUser()).thenReturn(mockAdminUser);
    when(superAdminUserService.assignUserRole(
            eq(1L), any(AssignUserRoleRequest.class), eq(mockAdminUser)))
        .thenReturn(userDto);

    ResponseEntity<SuperAdminUserDetailResponse> response = controller.assignUserRole(1L, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2L, response.getBody().getRole().getId());
  }

  @Test
  void testAssignMultipleUserRoles_Success() {
    RoleSimpleResponse r1 = RoleSimpleResponse.builder().id(2L).code("ROLE_GOVERNOR").build();
    RoleSimpleResponse r2 = RoleSimpleResponse.builder().id(3L).code("ROLE_HEAD_OFFICE").build();

    SuperAdminUserDetailResponse userDto =
        SuperAdminUserDetailResponse.builder()
            .id(1L)
            .username("superadmin")
            .role(r1)
            .roles(List.of(r1, r2))
            .build();

    AssignUserRoleRequest request =
        AssignUserRoleRequest.builder().roleIds(List.of(2L, 3L)).build();

    when(securityUtils.getCurrentUser()).thenReturn(mockAdminUser);
    when(superAdminUserService.assignUserRole(
            eq(1L), any(AssignUserRoleRequest.class), eq(mockAdminUser)))
        .thenReturn(userDto);

    ResponseEntity<SuperAdminUserDetailResponse> response = controller.assignUserRole(1L, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getRoles().size());
  }

  @Test
  void testResetUserPassword_Success() {
    AdminResetPasswordRequest request =
        AdminResetPasswordRequest.builder().newPassword("TemporaryPassword2026!").build();

    when(securityUtils.getCurrentUser()).thenReturn(mockAdminUser);

    ResponseEntity<java.util.Map<String, Object>> response =
        controller.resetUserPassword(1L, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(true, response.getBody().get("success"));
    assertEquals(1L, response.getBody().get("userId"));
    verify(superAdminUserService)
        .resetUserPassword(eq(1L), any(AdminResetPasswordRequest.class), eq(mockAdminUser));
  }
}
