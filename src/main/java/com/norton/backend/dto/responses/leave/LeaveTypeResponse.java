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
public class LeaveTypeResponse {

  @JsonProperty("key")
  private String key;

  @JsonProperty("label_en")
  private String labelEn;

  @JsonProperty("label_kh")
  private String labelKh;

  @JsonProperty("description")
  private String description;
}
