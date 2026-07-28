package com.norton.backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "leave_types",
    indexes = {@Index(name = "idx_leave_types_key", columnList = "type_key")})
public class LeaveTypeModel extends BaseIdModel {

  @Column(name = "type_key", nullable = false, unique = true, length = 100)
  private String key;

  @Column(name = "label_en", nullable = false, length = 150)
  private String labelEn;

  @Column(name = "label_kh", nullable = false, length = 150)
  private String labelKh;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;
}
