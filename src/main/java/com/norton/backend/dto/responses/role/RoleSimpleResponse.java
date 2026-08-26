package com.norton.backend.dto.responses.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSimpleResponse {

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
}
