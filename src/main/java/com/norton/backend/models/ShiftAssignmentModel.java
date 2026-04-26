package com.norton.backend.models;

import com.norton.backend.enums.ShiftAssignmentScope;
import com.norton.backend.enums.ShiftDayOfWeek;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shift_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignmentModel extends BaseIdModel {

  @ManyToOne
  @JoinColumn(name = "shift_id")
  private ShiftModel shift;

  @Enumerated(EnumType.STRING)
  private ShiftAssignmentScope scope;

  private Long scopeId;

  private String scopeName;

  @Enumerated(EnumType.STRING)
  private ShiftDayOfWeek dayOfWeek;

  private LocalDate effectiveFrom;

  private LocalDate effectiveTo;
}
