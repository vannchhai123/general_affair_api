package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity(name = "Attendance")
@Table(
    name = "attendance",
    uniqueConstraints = @UniqueConstraint(columnNames = {"officer_id", "date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceModel extends BaseIdModel {

  @NotNull
  @ManyToOne
  @JoinColumn(name = "officer_id", nullable = false)
  private OfficerModel officer;

  @NotNull private LocalDate date;

  private LocalDateTime checkIn;

  private LocalDateTime checkOut;

  @Min(0)
  private Integer totalWorkMin = 0;

  @Min(0)
  private Integer totalLateMin = 0;

  @ManyToOne
  @JoinColumn(name = "status_id")
  private AttendanceStatusModel status;

  @ManyToOne
  @JoinColumn(name = "approved_by")
  private OfficerModel approvedBy;

  @Column(columnDefinition = "TEXT")
  private String notes;
}
