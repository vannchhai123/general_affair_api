package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateQrSessionRequest {

  @NotNull(message = "created_by is required")
  @JsonProperty("created_by")
  private Long createdBy;

  @NotNull(message = "duration_seconds is required")
  @Min(value = 1, message = "duration_seconds must be greater than 0")
  @JsonProperty("duration_seconds")
  private Integer durationSeconds;

  @NotBlank(message = "location is required")
  private String location;
}
