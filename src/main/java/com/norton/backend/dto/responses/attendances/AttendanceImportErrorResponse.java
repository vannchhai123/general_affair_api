package com.norton.backend.dto.responses.attendances;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceImportErrorResponse {
  private int row;
  private String message;
}
