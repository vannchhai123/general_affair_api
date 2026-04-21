package com.norton.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Data;

@Data
public class AttendanceScanRequest {

  @NotBlank(message = "token is required")
  private String token;

  @NotBlank(message = "officerCode is required")
  private String officerCode;

  @NotBlank(message = "deviceId is required")
  private String deviceId;

  @NotNull(message = "scannedAt is required")
  private Instant scannedAt;
}
