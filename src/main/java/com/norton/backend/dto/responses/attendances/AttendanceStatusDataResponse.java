package com.norton.backend.dto.responses.attendances;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceStatusDataResponse {

  private Long officerId;

  @JsonProperty("isCheckedIn")
  private boolean isCheckedIn;

  @JsonProperty("isCheckedOut")
  private boolean isCheckedOut;

  private Instant checkInTime;
  private Instant checkOutTime;
  private String workingHours;
  private String shift;
  private String displayText;

  /**
   * The attendance date this record belongs to. Helps frontend verify which date's data is being
   * displayed. Useful for confirming auto-reset happened when moving to a new day.
   */
  private LocalDate attendanceDate;
}
