package com.norton.backend.controllers.superadmin;

import com.norton.backend.dto.responses.superadmin.AuditLogResponse;
import com.norton.backend.services.superadmin.AuditLogService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SuperAdminAuditLogController.BASE_URL)
public class SuperAdminAuditLogController {

  public static final String BASE_URL = "/api/v1/super-admin/audit-logs";

  private final AuditLogService auditLogService;

  @GetMapping
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
      @RequestParam(required = false) Long actorId,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant endDate) {
    return ResponseEntity.ok(
        auditLogService.getAuditLogs(actorId, action, entityType, startDate, endDate));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<AuditLogResponse> getAuditLogById(@PathVariable Long id) {
    return ResponseEntity.ok(auditLogService.getAuditLogById(id));
  }
}
