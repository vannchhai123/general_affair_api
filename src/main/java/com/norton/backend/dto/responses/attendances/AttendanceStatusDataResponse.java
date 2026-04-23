package com.norton.backend.dto.responses.attendances;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
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
}
