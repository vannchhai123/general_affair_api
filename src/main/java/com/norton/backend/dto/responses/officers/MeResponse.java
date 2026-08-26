package com.norton.backend.dto.responses.officers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {

  private String uuid;
  private String username;
  private String fullName;
  private String role;

  @JsonProperty("role_name_km")
  private String roleNameKm;

  @JsonProperty("role_name_en")
  private String roleNameEn;

  @JsonProperty("hierarchy_level")
  private Integer hierarchyLevel;

  private Long officerId;

  private OfficerResponse officer;
  private List<String> permissions;
}
