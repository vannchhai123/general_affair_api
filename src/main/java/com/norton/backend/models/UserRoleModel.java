package com.norton.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class UserRoleModel extends BaseIdModel {

  @Column(name = "role_name", nullable = false)
  private String roleName;

  private String description;

  @OneToMany(mappedBy = "role")
  private List<UserModel> users;
}
