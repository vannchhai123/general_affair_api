package com.norton.backend.models;

import com.norton.backend.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class UserModel extends BaseIdModel implements UserDetails {

  @NotBlank(message = "Full name is required")
  @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
  @Column(name = "full_name", nullable = false)
  private String fullName;

  @NotBlank(message = "Email is required")
  @Column(unique = true)
  private String username;

  @Email(message = "Invalid email format")
  @Column(unique = true, nullable = true)
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  @Column(name = "password", nullable = false)
  private String passwordHash;

  @NotNull(message = "User status is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus userStatus;

  @Singular("role")
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles_mapping",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private java.util.Set<UserRoleModel> roles = new java.util.HashSet<>();

  public UserRoleModel getPrimaryRole() {
    if (roles == null || roles.isEmpty()) {
      return null;
    }
    return roles.stream()
        .min(
            java.util.Comparator.comparing(
                r -> r.getHierarchyLevel() != null ? r.getHierarchyLevel() : 999))
        .orElse(roles.iterator().next());
  }

  public UserRoleModel getRole() {
    return getPrimaryRole();
  }

  public void setRole(UserRoleModel role) {
    if (this.roles == null) {
      this.roles = new java.util.HashSet<>();
    }
    if (role != null) {
      this.roles.clear();
      this.roles.add(role);
    }
  }

  @OneToOne(mappedBy = "user")
  private OfficerModel officer;

  @Column(name = "image_url", nullable = true)
  private String imageUrl;

  @Column(nullable = false, unique = true, updatable = false)
  private UUID uuid;

  @PrePersist
  public void generateUuid() {
    if (uuid == null) {
      uuid = UUID.randomUUID();
    }
  }

  @Transient private Collection<? extends GrantedAuthority> authorities;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (this.authorities == null) {
      List<GrantedAuthority> list = new ArrayList<>();
      if (roles != null && !roles.isEmpty()) {
        for (UserRoleModel r : roles) {
          if (r.getRoleName() != null) {
            list.add(new SimpleGrantedAuthority(r.getRoleName()));
          }
          if (r.getPermissions() != null) {
            r.getPermissions()
                .forEach(
                    permission ->
                        list.add(new SimpleGrantedAuthority(permission.getPermissionName())));
          }
        }
      }

      if (officer != null && officer.getOfficerPermissions() != null) {
        officer
            .getOfficerPermissions()
            .forEach(
                op -> {
                  if (op.getPermission() != null) {
                    list.add(new SimpleGrantedAuthority(op.getPermission().getPermissionName()));
                  }
                });
      }
      this.authorities = list;
    }
    return this.authorities;
  }

  @Override
  public @Nullable String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return userStatus == UserStatus.ACTIVE;
  }
}
