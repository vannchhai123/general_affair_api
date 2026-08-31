package com.norton.backend.controllers.superadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.responses.superadmin.SystemHealthResponse;
import com.norton.backend.services.superadmin.SuperAdminSystemService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SuperAdminSystemControllerTest {

  @Mock private SuperAdminSystemService systemService;

  @InjectMocks private SuperAdminSystemController controller;

  @Test
  void testGetSystemHealth_Success() {
    SystemHealthResponse.DatabaseHealthDto db =
        SystemHealthResponse.DatabaseHealthDto.builder()
            .status("UP")
            .latencyMs(12)
            .poolActive(4)
            .build();
    SystemHealthResponse.RedisHealthDto redis =
        SystemHealthResponse.RedisHealthDto.builder()
            .status("UP")
            .latencyMs(2)
            .memoryUsedMb(64)
            .build();
    SystemHealthResponse.DiskSpaceHealthDto disk =
        SystemHealthResponse.DiskSpaceHealthDto.builder()
            .status("UP")
            .freeGb(85)
            .totalGb(120)
            .build();

    SystemHealthResponse health =
        SystemHealthResponse.builder()
            .status("UP")
            .components(
                SystemHealthResponse.HealthComponentsDto.builder()
                    .db(db)
                    .redis(redis)
                    .diskSpace(disk)
                    .build())
            .uptime(1250320L)
            .build();

    when(systemService.getSystemHealth()).thenReturn(health);

    ResponseEntity<SystemHealthResponse> response = controller.getSystemHealth();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("UP", response.getBody().getStatus());
    assertEquals(12, response.getBody().getComponents().getDb().getLatencyMs());
    assertEquals(2, response.getBody().getComponents().getRedis().getLatencyMs());
    assertEquals(85, response.getBody().getComponents().getDiskSpace().getFreeGb());
    assertEquals(1250320L, response.getBody().getUptime());
  }

  @Test
  void testClearCache_Success() {
    when(systemService.clearCache())
        .thenReturn(
            Map.of(
                "success", true, "message", "All application caches were successfully flushed."));

    ResponseEntity<Map<String, Object>> response = controller.clearCache();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(true, response.getBody().get("success"));
  }
}
