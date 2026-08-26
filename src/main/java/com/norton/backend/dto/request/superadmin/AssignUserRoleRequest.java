package com.norton.backend.dto.request.superadmin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserRoleRequest {

  @NotNull(message = "Role ID is required")
  private Long roleId;
}
