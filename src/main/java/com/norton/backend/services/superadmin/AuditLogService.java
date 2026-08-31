package com.norton.backend.services.superadmin;

import com.norton.backend.dto.responses.superadmin.AuditLogResponse;
import java.time.Instant;
import java.util.List;

public interface AuditLogService {

  List<AuditLogResponse> getAuditLogs(
      Long actorId, String action, String entityType, Instant startDate, Instant endDate);

  AuditLogResponse getAuditLogById(Long id);
}
