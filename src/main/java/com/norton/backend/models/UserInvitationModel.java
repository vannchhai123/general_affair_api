package com.norton.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInvitationModel extends BaseIdModel {

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Column(name = "email", nullable = false)
  private String email;

  @NotBlank(message = "Full name is required")
  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "khmer_name")
  private String khmerName;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private UserRoleModel role;

  @Builder.Default
  @Column(name = "status", nullable = false, length = 50)
  private String status = "PENDING";

  @Column(name = "token", unique = true, length = 100)
  private String token;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invited_by_id")
  private UserModel invitedBy;

  @PrePersist
  public void prePersist() {
    if (this.token == null) {
      this.token = UUID.randomUUID().toString();
    }
    if (this.status == null) {
      this.status = "PENDING";
    }
    if (this.expiresAt == null) {
      this.expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
    }
  }
}
