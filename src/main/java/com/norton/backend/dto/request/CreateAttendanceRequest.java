package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class CreateAttendanceRequest {

  @NotNull(message = "officerId is required")
  @JsonAlias("officer_id")
  private Long officerId;

  @NotNull(message = "date is required")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  @NotNull(message = "checkIn is required")
  @JsonAlias("check_in")
  @JsonFormat(pattern = "HH:mm")
  private LocalTime checkIn;

  @NotNull(message = "checkOut is required")
  @JsonAlias("check_out")
  @JsonFormat(pattern = "HH:mm")
  private LocalTime checkOut;

  @NotBlank(message = "status is required")
  private String status;

  private String notes;
}
