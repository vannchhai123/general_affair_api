package com.norton.backend.dto.responses.attendances;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAttendanceResponse {
  private Long id;

  @JsonProperty("officer_id")
  private Long officerId;

  private LocalDate date;

  @JsonProperty("total_work_minutes")
  private Integer totalWorkMinutes;

  @JsonProperty("total_late_minutes")
  private Integer totalLateMinutes;

  private String status;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  private String department;

  @JsonProperty("approved_by")
  private Long approvedBy;

  @JsonProperty("approved_at")
  private Instant approvedAt;
}
