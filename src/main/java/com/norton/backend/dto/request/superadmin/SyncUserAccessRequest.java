package com.norton.backend.dto.request.superadmin;

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
public class SyncUserAccessRequest {

  @JsonProperty("roleIds")
  private List<Long> roleIds;

  @JsonProperty("directPermissions")
  private List<String> directPermissions;

  private String reason;
}
