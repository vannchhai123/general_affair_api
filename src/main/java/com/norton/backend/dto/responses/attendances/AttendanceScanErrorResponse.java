package com.norton.backend.dto.responses.attendances;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceScanErrorResponse {
  private boolean success;
  private String message;
  private String code;
}
