package com.norton.backend.dto.responses.attendances;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerReportResponse {
  private String officerId;
  private String name;
  private String role;
  private Long departmentId;
  private String departmentName;
  private String imageUrl;
  private String phone;
  private String email;
  private Attendance attendance;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Attendance {
    private LocalDate date;
    private String status;
    private String checkInTime;
    private String checkOutTime;
    private Integer workingHours;
    private Integer lateMinutes;
  }
}
