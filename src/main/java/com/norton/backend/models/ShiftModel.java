package com.norton.backend.models;

import com.norton.backend.enums.ShiftStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.*;

@Entity
@Table(name = "shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftModel extends BaseIdModel {

  @Size(max = 50)
  private String name;

  @Size(max = 50)
  private String code;

  private LocalTime startTime;

  private LocalTime endTime;

  @Enumerated(EnumType.STRING)
  private ShiftStatus status;

  private Boolean isActive;

  private Boolean crossMidnight;

  private Integer graceMinutes;

  private Integer checkInOpenBeforeMinutes;

  private Integer checkOutCloseAfterMinutes;

  private LocalDate effectiveFrom;

  private LocalDate effectiveTo;

  @Size(max = 500)
  private String description;
}
