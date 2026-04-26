package com.norton.backend.dto.responses.shifts;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftAssignmentTemplateResponseDto {
  private Long id;
  private Long shiftId;
  private String scope;
  private Long scopeId;
  private String scopeName;
  private LocalDate effectiveFrom;
  private LocalDate effectiveTo;
  private Map<String, List<Long>> days;
}
