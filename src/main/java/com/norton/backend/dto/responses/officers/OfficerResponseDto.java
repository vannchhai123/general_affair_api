package com.norton.backend.dto.responses.officers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Data;

@Data
public class OfficerResponseDto {
  private Long id;

  @JsonProperty("user_id")
  private Long userId;

  @JsonProperty("office_id")
  private Long officeId;

  @JsonProperty("position_id")
  private Long positionId;

  @JsonProperty("education_level_id")
  private Long educationLevelId;

  private String uuid;
  private String officerCode;

  @JsonProperty("first_name_en")
  private String firstName;

  @JsonProperty("last_name_en")
  private String lastName;

  @JsonProperty("first_name_kh")
  private String firstNameKh;

  @JsonProperty("last_name_kh")
  private String lastNameKh;

  @JsonProperty("date_of_birth")
  private LocalDate dateOfBirth;

  @JsonProperty("national_id")
  private String nationalId;

  private String nationality;
  private String ethnicity;

  private String email;
  private String sex;

  @JsonProperty("image_url")
  private String imageUrl;

  private String position;
  private String department;
  private String phone;

  @JsonProperty("hire_date")
  private LocalDate hireDate;

  @JsonProperty("contract_type")
  private String contractType;

  private String status;
  private String username;

  @JsonProperty("invitation_priority")
  private Boolean invitationPriority;

  @JsonProperty("office")
  public String getOffice() {
    return department;
  }
}
