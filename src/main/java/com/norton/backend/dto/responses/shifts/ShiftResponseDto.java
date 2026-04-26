package com.norton.backend.dto.responses.shifts;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftResponseDto {
  private Long id;
  private String name;
  private String code;
  private String startTime;
  private String endTime;
  private String status;
  private Boolean isActive;
  private Boolean crossMidnight;
  private Integer graceMinutes;
  private Integer checkInOpenBeforeMinutes;
  private Integer checkOutCloseAfterMinutes;
  private LocalDate effectiveFrom;
  private LocalDate effectiveTo;
  private String description;
  private long assignedDepartmentsCount;
  private long assignedPositionsCount;
  private long assignedEmployeesCount;
  private Instant createdAt;
  private Instant updatedAt;
}
