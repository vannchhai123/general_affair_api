package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.*;

@Entity
@Table(
    name = "attendance_sessions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"attendance_id", "shift_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSessionModel extends BaseIdModel {

  @NotBlank
  @Column(nullable = false, unique = true, length = 36)
  private String uuid;

  @ManyToOne
  @JoinColumn(name = "attendance_id")
  private AttendanceModel attendance;

  @ManyToOne
  @JoinColumn(name = "shift_id")
  private ShiftModel shift;

  private LocalTime checkIn;

  private LocalTime checkOut;

  @Size(max = 30)
  private String status;

  @ManyToOne
  @JoinColumn(name = "created_by")
  private OfficerModel createdBy;
}
