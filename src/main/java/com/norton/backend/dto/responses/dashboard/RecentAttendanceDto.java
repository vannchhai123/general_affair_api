package com.norton.backend.dto.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentAttendanceDto {
  private Long id;

  @JsonProperty("officerId")
  private Long officerId;

  @JsonProperty("imageUrl")
  private String imageUrl;

  private String date;

  @JsonProperty("checkIn")
  private String checkIn;

  @JsonProperty("checkOut")
  private String checkOut;

  @JsonProperty("totalWorkMin")
  private Integer totalWorkMin;

  @JsonProperty("totalLateMin")
  private Integer totalLateMin;

  private String status;

  @JsonProperty("firstName")
  private String firstName;

  @JsonProperty("lastName")
  private String lastName;

  private String department;

  @JsonProperty("officerCode")
  private String officerCode;

  private List<RecentAttendanceSessionDto> sessions;
}
