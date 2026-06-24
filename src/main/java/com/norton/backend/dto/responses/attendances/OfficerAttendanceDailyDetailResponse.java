package com.norton.backend.dto.responses.attendances;

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
public class OfficerAttendanceDailyDetailResponse {

  private LocalDate date;
  private String status;
  private LocalTime checkIn;
  private LocalTime checkOut;
  private String workingHours;
  private Integer lateMinutes;
  private OfficeInfo office;
  private LocationInfo location;
  private List<TimelineEntry> timeline;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OfficeInfo {

    private Long id;
    private String name;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LocationInfo {

    private Double lat;
    private Double lng;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TimelineEntry {

    private LocalTime time;
    private String type; // check_in, check_out
  }
}
