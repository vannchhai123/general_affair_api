package com.norton.backend.dto.responses.attendances;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceScanDataResponse {
  private Long attendanceId;
  private String sessionId;
  private Long officerId;
  private String officerName;
  private String action;
  private String status;
  private Instant scannedAt;
  private String location;
}
