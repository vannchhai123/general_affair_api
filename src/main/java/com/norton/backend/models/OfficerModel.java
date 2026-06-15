package com.norton.backend.models;

import com.norton.backend.enums.GenderEnum;
import com.norton.backend.enums.OfficerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
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
@Table(name = "officers")
public class OfficerModel extends BaseIdModel {

  @Column(name = "uuid", nullable = false, unique = true, length = 36)
  private String uuid;

  @NotBlank(message = "Officer code is required")
  @Size(max = 20)
  @Column(name = "officer_code", length = 20, nullable = false, unique = true)
  private String officerCode;

  @NotBlank(message = "First name in English is required")
  @Size(max = 100)
  @Column(name = "first_name_en", length = 100, nullable = false)
  private String firstNameEn;

  @NotBlank(message = "Last name in English is required")
  @Size(max = 100)
  @Column(name = "last_name_en", length = 100, nullable = false)
  private String lastNameEn;

  @NotBlank(message = "First name in Khmer is required")
  @Size(max = 100)
  @Column(name = "first_name_kh", length = 100, nullable = false)
  private String firstNameKh;

  @NotBlank(message = "Last name in Khmer is required")
  @Size(max = 100)
  @Column(name = "last_name_kh", length = 100, nullable = false)
  private String lastNameKh;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender", length = 20)
  private GenderEnum gender;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Size(max = 50)
  @Column(name = "national_id", length = 50, unique = true)
  private String nationalId;

  @Builder.Default
  @Size(max = 50)
  @Column(name = "nationality", length = 50)
  private String nationality = "Cambodian";

  @Builder.Default
  @Size(max = 50)
  @Column(name = "ethnicity", length = 50)
  private String ethnicity = "Cambodian";

  @Size(max = 100)
  @Pattern(regexp = "^[0-9+\\-() ]*$", message = "Invalid phone number format")
  private String phone;

  @Email(message = "Invalid email format")
  @Column(length = 150, unique = true, nullable = true)
  private String email;

  @Column(name = "image_url", length = 255)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private OfficerStatus status;

  @NotNull(message = "Office is required")
  @ManyToOne
  @JoinColumn(name = "office_id", nullable = false)
  private DepartmentModel office;

  @NotNull(message = "Position is required")
  @ManyToOne
  @JoinColumn(name = "position_id", nullable = false)
  private PositionModel position;

  @ManyToOne
  @JoinColumn(name = "education_level_id")
  private EducationLevelModel educationLevel;

  @NotNull(message = "Hire date is required")
  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @Size(max = 50)
  @Column(name = "contract_type", length = 50)
  private String contractType;

  @OneToMany(mappedBy = "officer", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OfficerPermission> officerPermissions = new ArrayList<>();

  @OneToMany(mappedBy = "officer", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OfficerAddressModel> addresses = new ArrayList<>();

  @OneToOne
  @JoinColumn(name = "user_id", unique = true)
  private UserModel user;

  @PrePersist
  public void prePersist() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
    if (this.nationality == null || this.nationality.isBlank()) {
      this.nationality = "Cambodian";
    }
    if (this.ethnicity == null || this.ethnicity.isBlank()) {
      this.ethnicity = "Cambodian";
    }
    if (this.status == null) {
      this.status = OfficerStatus.ACTIVE;
    }
  }

  public String getFirstName() {
    return firstNameEn;
  }

  public void setFirstName(String firstName) {
    this.firstNameEn = firstName;
  }

  public String getLastName() {
    return lastNameEn;
  }

  public void setLastName(String lastName) {
    this.lastNameEn = lastName;
  }
}
