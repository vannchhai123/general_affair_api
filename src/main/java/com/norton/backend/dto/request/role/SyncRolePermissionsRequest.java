package com.norton.backend.dto.request.role;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty("permission_ids")
  @JsonAlias({"permission_ids", "permissionIds"})
  private List<Long> permissionIds;
}
