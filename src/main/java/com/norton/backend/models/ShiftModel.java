package com.norton.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
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

  private LocalTime startTime;

  private LocalTime endTime;

  private Boolean isActive;
}
