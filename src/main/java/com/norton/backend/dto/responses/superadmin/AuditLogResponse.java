package com.norton.backend.dto.responses.superadmin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

  private Long id;

  @JsonProperty("actorId")
  private Long actorId;

  @JsonProperty("actorName")
  private String actorName;

  @JsonProperty("actorEmail")
  private String actorEmail;

  private String action;

  @JsonProperty("entityType")
  private String entityType;

  @JsonProperty("entityId")
  private Long entityId;

  @JsonProperty("ipAddress")
  private String ipAddress;

  @JsonProperty("userAgent")
  private String userAgent;

  private String details;

  @JsonProperty("stateBefore")
  private Map<String, Object> stateBefore;

  @JsonProperty("stateAfter")
  private Map<String, Object> stateAfter;

  private Instant timestamp;
}
