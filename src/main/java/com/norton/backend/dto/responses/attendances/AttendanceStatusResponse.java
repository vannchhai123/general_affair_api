package com.norton.backend.dto.responses.attendances;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceStatusResponse {
  private boolean success;
  private String message;
  private AttendanceStatusDataResponse data;
}
