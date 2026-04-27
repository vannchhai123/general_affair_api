package com.norton.backend.dto.responses.attendances;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceImportResponse {
  private int created;
  private int updated;
  private int failed;
  private List<AttendanceImportErrorResponse> errors;
}
