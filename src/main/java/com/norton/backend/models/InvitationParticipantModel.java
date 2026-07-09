package com.norton.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "invitation_participants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"invitation_id", "officer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationParticipantModel extends BaseIdModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invitation_id", nullable = false)
  private InvitationModel invitation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "officer_id", nullable = false)
  private OfficerModel officer;

  @Builder.Default
  @Column(name = "status", length = 50, nullable = false)
  private String status = "PENDING";

  @Column(name = "rejection_reason", length = 512)
  private String rejectionReason;
}
