package com.norton.backend.controllers.superadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.responses.permissions.PermissionResponse;
import com.norton.backend.mapper.PermissionMapper;
import com.norton.backend.models.PermissionModel;
import com.norton.backend.repositories.PermissionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SuperAdminPermissionControllerTest {

  @Mock private PermissionRepository permissionRepository;
  @Mock private PermissionMapper permissionMapper;

  @InjectMocks private SuperAdminPermissionController controller;

  @Test
  void testGetAllPermissions_Success() {
    PermissionModel p1 =
        PermissionModel.builder()
            .permissionName("ALL_PERMISSIONS")
            .description("Unrestricted master access across all platform services")
            .category("SYSTEM")
            .build();
    p1.setId(1L);

    PermissionResponse r1 =
        PermissionResponse.builder()
            .id(1L)
            .name("Full System Super Admin")
            .code("ALL_PERMISSIONS")
            .category("SYSTEM")
            .description("Unrestricted master access across all platform services")
            .build();

    when(permissionRepository.findAll(any(Sort.class))).thenReturn(List.of(p1));
    when(permissionMapper.toResponseList(List.of(p1))).thenReturn(List.of(r1));

    ResponseEntity<List<PermissionResponse>> response = controller.getAllPermissions();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("ALL_PERMISSIONS", response.getBody().get(0).getCode());
  }
}
