package com.norton.backend.dto.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentAttendanceSessionDto {
  private Long id;

  @JsonProperty("shiftName")
  private String shiftName;

  @JsonProperty("checkIn")
  private String checkIn;

  @JsonProperty("checkOut")
  private String checkOut;

  private String status;
}
