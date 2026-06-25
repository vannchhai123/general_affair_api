package com.norton.backend.dto.responses.attendances;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.norton.backend.dto.responses.organization.DepartmentResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllOfficersReportResponse {

  private Summary summary;

  @JsonProperty("officerAllowViewDepartmetn")
  private List<DepartmentResponseDto> officerAllowViewDepartmetn;

  private List<AttendanceStaffReportItem> attendanceStaffs;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Summary {

    private int totalStaff;
    private int presentCount;
    private int absentCount;
    private int attendancePercentage;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AttendanceStaffReportItem {

    private String officerId;
    private String name;
    private String role;
    private Long departmentId;
    private String departmentName;
    private boolean isPresent;
    private String imageUrl;
    private String checkInTime;
    private String checkOutTime;
  }
}
