package com.norton.backend.dto.responses.attendances;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceSummaryDataResponse {
  private Long officerId;
  private int presentCount;
  private int absentCount;
  private int lateCount;
  private int totalWorkingDays;
}
