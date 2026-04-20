package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Data;

@Data
public class CreateQrSessionCheckInRequest {

  @NotNull(message = "employee_id is required")
  @JsonProperty("employee_id")
  private Long employeeId;

  @NotBlank(message = "action is required")
  private String action;

  @NotNull(message = "timestamp is required")
  private Instant timestamp;
}
