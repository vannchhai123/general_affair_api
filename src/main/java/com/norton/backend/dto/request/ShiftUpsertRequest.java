package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ShiftUpsertRequest {
  @NotBlank(message = "name is required")
  @Size(max = 50, message = "name must not exceed 50 characters")
  private String name;

  @NotBlank(message = "code is required")
  @Size(max = 50, message = "code must not exceed 50 characters")
  private String code;

  @JsonAlias("start_time")
  @NotBlank(message = "startTime is required")
  @Pattern(
      regexp = "^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$",
      message = "startTime must be HH:mm or HH:mm:ss")
  private String startTime;

  @JsonAlias("end_time")
  @NotBlank(message = "endTime is required")
  @Pattern(
      regexp = "^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$",
      message = "endTime must be HH:mm or HH:mm:ss")
  private String endTime;

  @JsonAlias("cross_midnight")
  private Boolean crossMidnight;

  @JsonAlias("grace_minutes")
  @Min(value = 0, message = "graceMinutes must be >= 0")
  private Integer graceMinutes;

  @JsonAlias("check_in_open_before_minutes")
  @Min(value = 0, message = "checkInOpenBeforeMinutes must be >= 0")
  private Integer checkInOpenBeforeMinutes;

  @JsonAlias("check_out_close_after_minutes")
  @Min(value = 0, message = "checkOutCloseAfterMinutes must be >= 0")
  private Integer checkOutCloseAfterMinutes;

  @NotBlank(message = "status is required")
  private String status;

  @JsonAlias("is_active")
  private Boolean isActive;

  @JsonAlias("effective_from")
  @NotNull(message = "effectiveFrom is required")
  private LocalDate effectiveFrom;

  @JsonAlias("effective_to")
  private LocalDate effectiveTo;

  @Size(max = 500, message = "description must not exceed 500 characters")
  private String description;
}
