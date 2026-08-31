package com.norton.backend.services.superadmin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.norton.backend.dto.responses.superadmin.AuditLogResponse;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.AuditLogModel;
import com.norton.backend.repositories.AuditLogRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<AuditLogResponse> getAuditLogs(
      Long actorId, String action, String entityType, Instant startDate, Instant endDate) {
    List<AuditLogModel> logs =
        auditLogRepository.searchAuditLogs(actorId, action, entityType, startDate, endDate);
    return logs.stream().map(this::toResponse).toList();
  }

  @Override
  public AuditLogResponse getAuditLogById(Long id) {
    AuditLogModel logModel =
        auditLogRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
    return toResponse(logModel);
  }

  private AuditLogResponse toResponse(AuditLogModel model) {
    Map<String, Object> stateBefore = parseJsonMap(model.getStateBefore());
    Map<String, Object> stateAfter = parseJsonMap(model.getStateAfter());

    return AuditLogResponse.builder()
        .id(model.getId())
        .actorId(model.getActorId())
        .actorName(model.getActorName())
        .actorEmail(model.getActorEmail())
        .action(model.getAction())
        .entityType(model.getEntityType())
        .entityId(model.getEntityId())
        .ipAddress(model.getIpAddress())
        .userAgent(model.getUserAgent())
        .details(model.getDetails())
        .stateBefore(stateBefore)
        .stateAfter(stateAfter)
        .timestamp(model.getTimestamp())
        .build();
  }

  private Map<String, Object> parseJsonMap(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      log.warn("Failed to parse audit log state json: {}", json, e);
      return Collections.emptyMap();
    }
  }
}
