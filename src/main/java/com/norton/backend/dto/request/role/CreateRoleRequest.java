package com.norton.backend.dto.request.role;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {

  @NotBlank(message = "Role code is required")
  @Pattern(
      regexp = "^[A-Za-z0-9_]+$",
      message = "Role code must contain only letters, numbers, and underscores")
  private String code;

  @NotBlank(message = "Khmer role name is required")
  @JsonProperty("name_km")
  @JsonAlias({"name_km", "nameKm"})
  private String nameKm;

  @JsonProperty("name_en")
  @JsonAlias({"name_en", "nameEn"})
  private String nameEn;

  @NotNull(message = "Hierarchy level is required")
  @Min(value = 1, message = "Hierarchy level must be at least 1")
  @JsonProperty("hierarchy_level")
  @JsonAlias({"hierarchy_level", "hierarchyLevel"})
  private Integer hierarchyLevel;

  private String description;

  @JsonProperty("permission_ids")
  @JsonAlias({"permission_ids", "permissionIds"})
  private List<Long> permissionIds;
}
