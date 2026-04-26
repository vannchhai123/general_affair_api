package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShiftStatusPatchRequest {
  @NotBlank(message = "status is required")
  @JsonAlias("shift_status")
  private String status;
}
