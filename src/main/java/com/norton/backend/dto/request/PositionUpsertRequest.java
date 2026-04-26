package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PositionUpsertRequest {
  @NotBlank(message = "title is required")
  @Size(max = 100, message = "title must not exceed 100 characters")
  private String title;

  @NotBlank(message = "code is required")
  @Size(max = 50, message = "code must not exceed 50 characters")
  private String code;

  @NotNull(message = "department_id is required")
  @JsonProperty("department_id")
  private Long departmentId;

  @NotBlank(message = "status is required")
  private String status;

  @Size(max = 500, message = "description must not exceed 500 characters")
  private String description;
}
