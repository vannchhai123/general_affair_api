package com.norton.backend.dto.responses.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentResponseDto {
  private Long id;
  private String uuid;
  private String name;

  @JsonProperty("name_kh")
  private String nameKh;

  private String code;
  private String manager;
  private Long adminId;
  private String adminName;
  private String adminUsername;

  @JsonProperty("officer_count")
  private long officerCount;

  private String status;
  private String description;
}
