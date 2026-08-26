package com.norton.backend.dto.request.role;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncRolePermissionsRequest {

  @NotNull(message = "Permission IDs list cannot be null")
  private List<Long> permissionIds;
}
