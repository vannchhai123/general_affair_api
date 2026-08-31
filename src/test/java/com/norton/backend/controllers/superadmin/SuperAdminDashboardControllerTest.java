package com.norton.backend.controllers.superadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.responses.superadmin.SuperAdminStatsResponse;
import com.norton.backend.services.superadmin.SuperAdminDashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SuperAdminDashboardControllerTest {

  @Mock private SuperAdminDashboardService superAdminDashboardService;

  @InjectMocks private SuperAdminDashboardController controller;

  @Test
  void testGetSystemStats_Success() {
    SuperAdminStatsResponse stats =
        SuperAdminStatsResponse.builder()
            .totalUsers(142)
            .activeUsers(128)
            .bannedUsers(14)
            .totalOfficers(95)
            .totalRoles(8)
            .totalPermissions(64)
            .totalDepartments(12)
            .totalPositions(24)
            .build();

    when(superAdminDashboardService.getSystemStats()).thenReturn(stats);

    ResponseEntity<SuperAdminStatsResponse> response = controller.getSystemStats();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(142, response.getBody().getTotalUsers());
    assertEquals(128, response.getBody().getActiveUsers());
    assertEquals(14, response.getBody().getBannedUsers());
    assertEquals(95, response.getBody().getTotalOfficers());
    assertEquals(8, response.getBody().getTotalRoles());
    assertEquals(64, response.getBody().getTotalPermissions());
    assertEquals(12, response.getBody().getTotalDepartments());
    assertEquals(24, response.getBody().getTotalPositions());
  }
}
