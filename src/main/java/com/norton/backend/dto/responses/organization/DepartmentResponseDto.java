package com.norton.backend.dto.responses.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponseDto {
  private Long id;
  private String uuid;
  private String name;
  private String code;
  private String manager;

  @JsonProperty("officer_count")
  private long officerCount;

  private String status;
  private String description;
}
