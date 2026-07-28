package com.norton.backend.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "leave_requests")
public class LeaveRequestModel extends BaseIdModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "officer_id", nullable = false)
  private OfficerModel officer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approved_by")
  private OfficerModel approvedByOfficer;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leave_type_id")
  private LeaveTypeModel leaveType;

  @Column(name = "total_days", nullable = false)
  private int totalDays;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;

  @Column(name = "status", nullable = false, length = 50)
  private String status;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;
}
