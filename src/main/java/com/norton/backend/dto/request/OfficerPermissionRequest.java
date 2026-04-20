package com.norton.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class OfficerPermissionRequest {

  @NotNull private Long officerId;

  @NotNull private Long permissionId;
}
