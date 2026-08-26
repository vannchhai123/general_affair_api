package com.norton.backend.dto.request.role;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

  @NotBlank(message = "Khmer role name is required")
  private String nameKm;

  private String nameEn;

  @NotNull(message = "Hierarchy level is required")
  @Min(value = 1, message = "Hierarchy level must be at least 1")
  private Integer hierarchyLevel;

  private String description;
}
