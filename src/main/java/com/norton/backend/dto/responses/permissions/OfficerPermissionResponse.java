package com.norton.backend.dto.responses.permissions;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfficerPermissionResponse {

  private Long id;
  private Long officerId;
  private Long permissionId;
  private LocalDateTime grantedAt;
  private Long grantedBy;
}
