package com.norton.backend.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionResponse {

  private Long id;

  @JsonProperty("permission_name")
  private String permissionName;

  private String description;
  private String category;
}
