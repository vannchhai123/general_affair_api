package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "qr_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrSessionModel extends BaseIdModel {

  @NotBlank
  @Column(nullable = false, unique = true)
  private String token;

  @Size(max = 50)
  private String status;

  private String location;

  private LocalDateTime validUntil;

  @Min(0)
  private Integer qrRefreshInterval;

  @ManyToOne
  @JoinColumn(name = "created_by")
  private OfficerModel createdBy;

  private LocalDateTime startedAt;
  private LocalDateTime stoppedAt;
}
