package com.norton.backend.dto.responses.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionResponseDto {
  private Long id;
  private String uuid;
  private String title;
  private String code;

  @JsonProperty("department_id")
  private Long departmentId;

  @JsonProperty("department_name")
  private String departmentName;

  @JsonProperty("officer_count")
  private long officerCount;

  private String status;
  private String description;
}
