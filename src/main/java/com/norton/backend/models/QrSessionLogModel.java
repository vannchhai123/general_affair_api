package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "qr_session_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrSessionLogModel extends BaseIdModel {

  @NotNull
  @ManyToOne
  @JoinColumn(name = "qr_session_id", nullable = false)
  private QrSessionModel qrSession;

  @Size(max = 100)
  private String action;

  @ManyToOne
  @JoinColumn(name = "performed_by")
  private OfficerModel performedBy;

  @Column(columnDefinition = "TEXT")
  private String details;
}
