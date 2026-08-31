package com.norton.backend.controllers.superadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.permissions.PermissionResponse;
import com.norton.backend.dto.responses.role.RoleResponse;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.services.role.RoleService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SuperAdminRoleControllerTest {

  @Mock private RoleService roleService;
  @Mock private com.norton.backend.utils.SecurityUtils securityUtils;

  @InjectMocks private SuperAdminRoleController controller;

  @Test
  void testGetAllRoles_Success() {
    RoleResponse roleDto =
        RoleResponse.builder()
            .id(1L)
            .code("ROLE_SUPER_ADMIN")
            .nameKm("អភិបាលជាន់ខ្ពស់")
            .nameEn("Super Administrator")
            .hierarchyLevel(100)
            .isSystem(true)
            .description("Maximum authority to manage the entire platform.")
            .userCount(2)
            .permissions(
                List.of(
                    PermissionResponse.builder()
                        .id(1L)
                        .name("All Permissions")
                        .code("ALL_PERMISSIONS")
                        .category("SYSTEM")
                        .build()))
            .createdAt(Instant.parse("2026-08-31T08:00:00Z"))
            .updatedAt(Instant.parse("2026-08-31T08:00:00Z"))
            .build();

    PageResponse<RoleResponse> pageResponse =
        PageResponse.<RoleResponse>builder()
            .content(List.of(roleDto))
            .totalElements(1)
            .totalPages(1)
            .page(0)
            .size(20)
            .first(true)
            .last(true)
            .empty(false)
            .build();

    Pageable pageable = PageRequest.of(0, 20);
    when(roleService.getAllRoles(eq(null), any(Pageable.class))).thenReturn(pageResponse);

    ResponseEntity<PageResponse<RoleResponse>> response = controller.getAllRoles(null, pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getTotalElements());
    assertEquals("ROLE_SUPER_ADMIN", response.getBody().getContent().get(0).getCode());
  }

  @Test
  void testGetSimpleRoles_Success() {
    RoleSimpleResponse r1 =
        RoleSimpleResponse.builder()
            .id(1L)
            .code("ROLE_SUPER_ADMIN")
            .nameKm("អភិបាលជាន់ខ្ពស់")
            .nameEn("Super Administrator")
            .hierarchyLevel(100)
            .build();
    RoleSimpleResponse r2 =
        RoleSimpleResponse.builder()
            .id(2L)
            .code("ROLE_GOVERNOR")
            .nameKm("អភិបាលខេត្ត")
            .nameEn("Provincial Governor")
            .hierarchyLevel(90)
            .build();

    when(roleService.getSimpleRoles()).thenReturn(List.of(r1, r2));

    ResponseEntity<List<RoleSimpleResponse>> response = controller.getSimpleRoles();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());
  }

  @Test
  void testGetRoleById_Success() {
    RoleResponse roleDto =
        RoleResponse.builder()
            .id(2L)
            .code("ROLE_GOVERNOR")
            .nameKm("អភិបាលខេត្ត")
            .nameEn("Provincial Governor")
            .hierarchyLevel(90)
            .build();

    when(roleService.getRoleById(2L)).thenReturn(roleDto);

    ResponseEntity<RoleResponse> response = controller.getRoleById(2L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("ROLE_GOVERNOR", response.getBody().getCode());
  }

  @Test
  void testCreateRole_Success() {
    com.norton.backend.dto.request.role.CreateRoleRequest request =
        com.norton.backend.dto.request.role.CreateRoleRequest.builder()
            .code("ROLE_FINANCE_DIRECTOR")
            .nameKm("ប្រធានមន្ទីរសេដ្ឋកិច្ច និងហិរញ្ញវត្ថុ")
            .nameEn("Director of Finance")
            .hierarchyLevel(70)
            .description("Manages provincial budget and financial approvals.")
            .permissionIds(List.of(2L, 3L, 5L))
            .build();

    RoleResponse roleDto =
        RoleResponse.builder()
            .id(3L)
            .code("ROLE_FINANCE_DIRECTOR")
            .nameKm("ប្រធានមន្ទីរសេដ្ឋកិច្ច និងហិរញ្ញវត្ថុ")
            .nameEn("Director of Finance")
            .hierarchyLevel(70)
            .build();

    com.norton.backend.models.UserModel mockUser =
        com.norton.backend.models.UserModel.builder().username("superadmin").build();
    when(securityUtils.getCurrentUser()).thenReturn(mockUser);
    when(roleService.createRole(any(), eq(mockUser))).thenReturn(roleDto);

    ResponseEntity<RoleResponse> response = controller.createRole(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("ROLE_FINANCE_DIRECTOR", response.getBody().getCode());
  }

  @Test
  void testUpdateRole_Success() {
    com.norton.backend.dto.request.role.UpdateRoleRequest request =
        com.norton.backend.dto.request.role.UpdateRoleRequest.builder()
            .nameKm("ប្រធានមន្ទីរសេដ្ឋកិច្ច និងហិរញ្ញវត្ថុ (កែប្រែ)")
            .nameEn("Director of Finance & Budgeting")
            .hierarchyLevel(75)
            .description("Updated description for finance head.")
            .build();

    RoleResponse roleDto =
        RoleResponse.builder()
            .id(3L)
            .code("ROLE_FINANCE_DIRECTOR")
            .nameKm("ប្រធានមន្ទីរសេដ្ឋកិច្ច និងហិរញ្ញវត្ថុ (កែប្រែ)")
            .nameEn("Director of Finance & Budgeting")
            .hierarchyLevel(75)
            .build();

    com.norton.backend.models.UserModel mockUser =
        com.norton.backend.models.UserModel.builder().username("superadmin").build();
    when(securityUtils.getCurrentUser()).thenReturn(mockUser);
    when(roleService.updateRole(eq(3L), any(), eq(mockUser))).thenReturn(roleDto);

    ResponseEntity<RoleResponse> response = controller.updateRole(3L, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Director of Finance & Budgeting", response.getBody().getNameEn());
  }

  @Test
  void testDeleteRole_Success() {
    com.norton.backend.models.UserModel mockUser =
        com.norton.backend.models.UserModel.builder().username("superadmin").build();
    when(securityUtils.getCurrentUser()).thenReturn(mockUser);

    ResponseEntity<java.util.Map<String, Object>> response = controller.deleteRole(3L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(true, response.getBody().get("success"));
  }

  @Test
  void testSyncRolePermissions_Success() {
    com.norton.backend.dto.request.role.SyncRolePermissionsRequest request =
        com.norton.backend.dto.request.role.SyncRolePermissionsRequest.builder()
            .permissionIds(List.of(1L, 2L, 3L, 5L, 8L))
            .build();

    RoleResponse roleDto = RoleResponse.builder().id(3L).code("ROLE_FINANCE_DIRECTOR").build();

    com.norton.backend.models.UserModel mockUser =
        com.norton.backend.models.UserModel.builder().username("superadmin").build();
    when(securityUtils.getCurrentUser()).thenReturn(mockUser);
    when(roleService.syncRolePermissionsAndReturn(
            eq(3L), eq(List.of(1L, 2L, 3L, 5L, 8L)), eq(mockUser)))
        .thenReturn(roleDto);

    ResponseEntity<RoleResponse> response = controller.syncRolePermissions(3L, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(3L, response.getBody().getId());
  }
}
