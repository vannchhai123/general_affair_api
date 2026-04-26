package com.norton.backend.dto.responses.shifts;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftAssignmentResponseDto {
  private Long id;
  private Long shiftId;
  private String scope;
  private Long scopeId;
  private String scopeName;
  private String dayOfWeek;
  private LocalDate effectiveFrom;
  private LocalDate effectiveTo;
}
