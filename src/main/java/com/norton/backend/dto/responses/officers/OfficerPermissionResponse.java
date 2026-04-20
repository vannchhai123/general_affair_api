package com.norton.backend.dto.responses.officers;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OfficerPermissionResponse {

  private Long id;
  private Long officerId;
  private Long permissionId;
  private LocalDateTime grantedAt;
  private Long grantedBy;

  private String officerName;
  private String officerDepartment;

  private String permissionName;
  private String permissionCategory;
}
