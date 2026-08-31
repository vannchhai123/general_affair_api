package com.norton.backend.controllers.superadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.responses.superadmin.AuditLogResponse;
import com.norton.backend.services.superadmin.AuditLogService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SuperAdminAuditLogControllerTest {

  @Mock private AuditLogService auditLogService;

  @InjectMocks private SuperAdminAuditLogController controller;

  @Test
  void testGetAuditLogs_Success() {
    AuditLogResponse logDto =
        AuditLogResponse.builder()
            .id(1L)
            .actorId(1L)
            .actorName("Super Admin")
            .actorEmail("superadmin@domain.gov.kh")
            .action("STATUS_CHANGE")
            .entityType("User")
            .entityId(2L)
            .ipAddress("192.168.1.100")
            .userAgent("Mozilla/5.0")
            .details("Changed status of user 'governor.kandal' to ACTIVE")
            .stateBefore(Map.of("status", "INACTIVE"))
            .stateAfter(Map.of("status", "ACTIVE"))
            .timestamp(Instant.parse("2026-08-31T08:30:00Z"))
            .build();

    when(auditLogService.getAuditLogs(eq(null), eq(null), eq(null), eq(null), eq(null)))
        .thenReturn(List.of(logDto));

    ResponseEntity<List<AuditLogResponse>> response =
        controller.getAuditLogs(null, null, null, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("STATUS_CHANGE", response.getBody().get(0).getAction());
    assertEquals("User", response.getBody().get(0).getEntityType());
  }

  @Test
  void testGetAuditLogById_Success() {
    AuditLogResponse logDto =
        AuditLogResponse.builder()
            .id(1L)
            .actorId(1L)
            .actorName("Super Admin")
            .action("STATUS_CHANGE")
            .build();

    when(auditLogService.getAuditLogById(1L)).thenReturn(logDto);

    ResponseEntity<AuditLogResponse> response = controller.getAuditLogById(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1L, response.getBody().getId());
  }
}
