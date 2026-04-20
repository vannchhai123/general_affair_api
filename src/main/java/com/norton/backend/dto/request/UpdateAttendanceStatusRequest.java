package com.norton.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAttendanceStatusRequest {

  @NotBlank(message = "status is required")
  private String status;
}
