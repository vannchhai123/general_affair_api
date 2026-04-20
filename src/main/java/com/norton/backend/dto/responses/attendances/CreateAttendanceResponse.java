package com.norton.backend.dto.responses.attendances;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateAttendanceResponse {
  private Long id;

  @JsonProperty("officer_id")
  private Long officerId;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  private String department;

  @JsonProperty("employee_code")
  private String employeeCode;

  private LocalDate date;

  @JsonProperty("check_in")
  private String checkIn;

  @JsonProperty("check_out")
  private String checkOut;

  @JsonProperty("total_work_minutes")
  private Integer totalWorkMinutes;

  @JsonProperty("total_late_minutes")
  private Integer totalLateMinutes;

  private String status;
  private List<AttendanceSessionResponse> sessions;
}
