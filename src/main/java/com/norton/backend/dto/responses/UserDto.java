package com.norton.backend.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  private UUID uuid;
  private String fullName;
  private String role;

  @JsonProperty("role_name_km")
  private String roleNameKm;

  @JsonProperty("role_name_en")
  private String roleNameEn;

  @JsonProperty("hierarchy_level")
  private Integer hierarchyLevel;

  private boolean enabled;
  private String imageUrl;
  private List<String> permissions;
}
