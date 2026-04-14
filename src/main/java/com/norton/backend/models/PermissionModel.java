package com.norton.backend.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionModel extends BaseIdModel {

  @Column(name = "per_name", nullable = false, unique = true)
  private String permissionName;

  @ManyToMany(mappedBy = "permissions")
  private Set<UserRoleModel> roles = new HashSet<>();

  private String description;

  private String category;
}
