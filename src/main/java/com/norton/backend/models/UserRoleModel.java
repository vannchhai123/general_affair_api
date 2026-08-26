package com.norton.backend.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class UserRoleModel extends BaseIdModel {

  @Column(name = "code", unique = true, length = 100)
  private String code;

  @Column(name = "role_name", unique = true, length = 100)
  private String roleName;

  @Column(name = "name_km", length = 150)
  private String nameKm;

  @Column(name = "name_en", length = 150)
  private String nameEn;

  @Builder.Default
  @Column(name = "hierarchy_level", nullable = false)
  private Integer hierarchyLevel = 99;

  @Builder.Default
  @Column(name = "is_system", nullable = false)
  private Boolean isSystem = false;

  @Column(name = "description", length = 500)
  private String description;

  @Builder.Default
  @OneToMany(mappedBy = "role")
  private List<UserModel> users = new ArrayList<>();

  @Builder.Default
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_permission",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<PermissionModel> permissions = new HashSet<>();

  public String getRoleName() {
    return this.code != null ? this.code : this.roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
    if (this.code == null) {
      this.code = roleName;
    }
  }

  @PrePersist
  @PreUpdate
  public void syncCodeAndDefaults() {
    if (this.code == null && this.roleName != null) {
      this.code = this.roleName;
    }
    if (this.roleName == null && this.code != null) {
      this.roleName = this.code;
    }
    if (this.hierarchyLevel == null) {
      this.hierarchyLevel = 99;
    }
    if (this.isSystem == null) {
      this.isSystem = false;
    }
    if (this.nameKm == null || this.nameKm.isBlank()) {
      this.nameKm = this.nameEn != null ? this.nameEn : this.getRoleName();
    }
  }
}
