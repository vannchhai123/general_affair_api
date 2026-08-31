package com.norton.backend.dto.request.superadmin;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class AssignUserRoleRequest {

  @JsonProperty("roleId")
  @JsonAlias({"roleId", "role_id"})
  private Long roleId;

  @JsonProperty("roleIds")
  @JsonAlias({"roleIds", "role_ids", "roles"})
  private List<Long> roleIds;
}
