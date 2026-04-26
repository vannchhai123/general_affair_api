package com.norton.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentUpsertRequest {
  @NotBlank(message = "name is required")
  @Size(max = 100, message = "name must not exceed 100 characters")
  private String name;

  @NotBlank(message = "code is required")
  @Size(max = 50, message = "code must not exceed 50 characters")
  private String code;

  @Size(max = 255, message = "manager must not exceed 255 characters")
  private String manager;

  @NotBlank(message = "status is required")
  private String status;

  @Size(max = 500, message = "description must not exceed 500 characters")
  private String description;
}
