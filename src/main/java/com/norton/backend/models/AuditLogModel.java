package com.norton.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogModel extends BaseIdModel {

  @Column(name = "actor_id")
  private Long actorId;

  @Column(name = "actor_name", length = 150)
  private String actorName;

  @Column(name = "actor_email", length = 150)
  private String actorEmail;

  @Column(name = "action", nullable = false, length = 50)
  private String action;

  @Column(name = "entity_type", length = 50)
  private String entityType;

  @Column(name = "entity_id")
  private Long entityId;

  @Column(name = "ip_address", length = 100)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "details", length = 1000)
  private String details;

  @Column(name = "state_before", columnDefinition = "TEXT")
  private String stateBefore;

  @Column(name = "state_after", columnDefinition = "TEXT")
  private String stateAfter;

  @Column(name = "timestamp", nullable = false)
  private Instant timestamp;

  @PrePersist
  public void prePersist() {
    if (this.timestamp == null) {
      this.timestamp = Instant.now();
    }
  }
}
