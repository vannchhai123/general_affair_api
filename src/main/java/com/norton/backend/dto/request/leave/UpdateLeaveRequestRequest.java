package com.norton.backend.dto.request.leave;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateLeaveRequestRequest {

  @JsonProperty("status")
  private String status;

  @JsonProperty("reason")
  private String reason;

  @JsonProperty("leave_type_id")
  private Long leaveTypeId;

  @JsonProperty("leave_type")
  private String leaveType;

  @JsonProperty("start_date")
  private String startDate;

  @JsonProperty("end_date")
  private String endDate;

  @JsonProperty("total_days")
  private Integer totalDays;
}
