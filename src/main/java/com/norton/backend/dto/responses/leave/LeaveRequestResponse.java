package com.norton.backend.dto.responses.leave;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponse {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("officer_id")
  private Long officerId;

  @JsonProperty("approved_by")
  private Long approvedBy;

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

  @JsonProperty("approved_at")
  private String approvedAt;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  @JsonProperty("department")
  private String department;

  @JsonProperty("approver_name")
  private String approverName;
}
