package com.norton.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "officer_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerPermission extends BaseIdModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "officer_id", nullable = false)
  private OfficerModel officer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permission_id", nullable = false)
  private PermissionModel permission;

  @Column(name = "granted_at")
  private LocalDateTime grantedAt;

  @Column(name = "granted_by")
  private Long grantedBy;
}
