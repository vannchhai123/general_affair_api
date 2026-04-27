package com.norton.backend.dto.responses.attendances;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceExportResponse {
  private String filename;
  private String contentType;
  private byte[] content;
}
