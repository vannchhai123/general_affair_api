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

  @NotNull(message = "Role is required")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private UserRoleModel role;

  @OneToOne(mappedBy = "user")
  private OfficerModel officer;

  @Column(nullable = false, unique = true, updatable = false)
  private UUID uuid;

  @PrePersist
  public void generateUuid() {
    if (uuid == null) {
      uuid = UUID.randomUUID();
    }
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {

    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

    role.getPermissions()
        .forEach(
            permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getPermissionName())));

    if (officer != null && officer.getOfficerPermissions() != null) {
      officer
          .getOfficerPermissions()
          .forEach(
              op -> {
                if (op.getPermission() != null) {
                  authorities.add(
                      new SimpleGrantedAuthority(op.getPermission().getPermissionName()));
                }
              });
    }
    return authorities;
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
