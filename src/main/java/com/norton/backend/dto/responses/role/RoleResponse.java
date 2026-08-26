package com.norton.backend.dto.responses.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.norton.backend.dto.responses.permissions.PermissionResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

  private Long id;
  private String code;

  @JsonProperty("name_km")
  private String nameKm;

  @JsonProperty("name_en")
  private String nameEn;

  @JsonProperty("hierarchy_level")
  private Integer hierarchyLevel;

  @JsonProperty("is_system")
  private Boolean isSystem;

  private String description;

  @JsonProperty("user_count")
  private Integer userCount;

  private List<PermissionResponse> permissions;

  @JsonProperty("created_at")
  private java.time.Instant createdAt;

  @JsonProperty("updated_at")
  private java.time.Instant updatedAt;
}
