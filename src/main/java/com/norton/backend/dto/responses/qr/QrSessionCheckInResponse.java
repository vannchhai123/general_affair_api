package com.norton.backend.dto.responses.qr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrSessionCheckInResponse {
  private Long id;

  @JsonProperty("employee_name")
  private String employeeName;

  @JsonProperty("employee_code")
  private String employeeCode;

  private String department;
  private String status;

  @JsonProperty("scanned_at")
  private Instant scannedAt;

  private String message;
}
