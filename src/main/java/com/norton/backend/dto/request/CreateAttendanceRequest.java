package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class CreateAttendanceRequest {

  @NotNull(message = "officer_id is required")
  @JsonProperty("officer_id")
  private Long officerId;

  @NotNull(message = "date is required")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  @NotNull(message = "check_in is required")
  @JsonProperty("check_in")
  @JsonFormat(pattern = "HH:mm")
  private LocalTime checkIn;

  @NotNull(message = "check_out is required")
  @JsonProperty("check_out")
  @JsonFormat(pattern = "HH:mm")
  private LocalTime checkOut;

  @NotBlank(message = "status is required")
  private String status;
  private String notes;
}
