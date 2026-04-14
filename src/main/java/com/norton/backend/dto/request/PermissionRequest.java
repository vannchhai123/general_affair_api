package com.norton.backend.dto.request;

import lombok.Data;

@Data
public class PermissionRequest {
  private String permissionName;
  private String description;
  private String category;
}
