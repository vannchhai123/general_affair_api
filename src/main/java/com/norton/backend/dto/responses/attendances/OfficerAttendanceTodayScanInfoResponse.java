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
public class OfficerAttendanceTodayScanInfoResponse {

  private LocalDate date;
  private LocalTime checkIn;
  private LocalTime checkOut;
  private String workingDuration;
  private String status;
  private List<TimelineEntry> timeline;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TimelineEntry {

    private String time; // Can be a time or a time range like "09:52" or "11:00 - 11:15"
    private String title; // Check In, Lunch Break, Meeting, etc.
    private String type; // check_in, break, activity, check_out
  }
}
