package com.norton.backend.dto.request.leave;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateLeaveRequestRequest {

  @JsonProperty("officer_id")
  private Long officerId;

  @JsonProperty("start_date")
  private String startDate;

  @JsonProperty("end_date")
  private String endDate;

  @JsonProperty("leave_type")
  private String leaveType;

  @JsonProperty("total_days")
  private Integer totalDays;

  @JsonProperty("reason")
  private String reason;

  @JsonProperty("status")
  private String status;
}
