package com.norton.backend.dto.request.leave;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateLeaveRequestRequest {

  @JsonProperty("status")
  private String status;

  @JsonProperty("reason")
  private String reason;
}
