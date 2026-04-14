package com.norton.backend.models;

import com.norton.backend.enums.GenderEnum;
import com.norton.backend.enums.OfficerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "officer")
public class OfficerModel extends BaseIdModel {

  @Column(name = "uuid", nullable = false, unique = true, length = 36)
  private String uuid;

  @NotBlank(message = "Officer code is required")
  @Size(max = 20)
  @Column(name = "officer_code", length = 20, nullable = false, unique = true)
  private String officerCode;

  @NotBlank(message = "First name is required")
  @Size(max = 255)
  @Column(name = "first_name", length = 255)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 255)
  @Column(name = "last_name", length = 255)
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender", length = 10)
  private GenderEnum gender;

  @Size(max = 100)
  @Pattern(regexp = "^[0-9+\\-() ]*$", message = "Invalid phone number format")
  private String phone;

  @Email(message = "Invalid email format")
  @Column(unique = true, nullable = true)
  private String email;

  @Column(name = "image_url", length = 255)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private OfficerStatus status;

  @NotNull(message = "Position is required")
  @ManyToOne
  @JoinColumn(name = "position_id", nullable = false)
  private PositionModel position;

  @OneToMany(mappedBy = "officer", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OfficerPermission> officerPermissions = new ArrayList<>();

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserModel user;

  @PrePersist
  public void generateUuid() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
  }
}
