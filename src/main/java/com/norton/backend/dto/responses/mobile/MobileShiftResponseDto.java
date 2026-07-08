package com.norton.backend.dto.responses.mobile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileShiftResponseDto {
  private boolean assigned;
  private List<ShiftItem> shifts;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ShiftItem {
    private ShiftDetails shift;
    private AssignmentDetails assignment;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ShiftDetails {
    private Long id;
    private String name;
    private String code;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer graceMinutes;
    private Integer checkInOpenBeforeMinutes;
    private Integer checkOutCloseAfterMinutes;
    private Boolean crossMidnight;
    private String description;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AssignmentDetails {
    private String scope;
    private Long scopeId;
    private String scopeName;
    private String dayOfWeek;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
  }
}
