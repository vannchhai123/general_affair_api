package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "qr_session_checkins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrSessionCheckInModel extends BaseIdModel {

  @NotNull
  @ManyToOne
  @JoinColumn(name = "qr_session_id", nullable = false)
  private QrSessionModel qrSession;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "officer_id", nullable = false)
  private OfficerModel officer;

  @Size(max = 50)
  private String action;

  @Size(max = 50)
  private String status;

  private LocalDateTime scannedAt;

  @Column(columnDefinition = "TEXT")
  private String deviceInfo;
}
