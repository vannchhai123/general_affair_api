package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateOfficerRequest {

  @NotBlank(message = "officerCode is required")
  @JsonProperty("officerCode")
  private String officerCode;

  @NotBlank(message = "first_name is required")
  @JsonAlias({"first_name", "firstName", "first_name_en"})
  @JsonProperty("first_name_en")
  private String firstNameEn;

  @NotBlank(message = "last_name is required")
  @JsonAlias({"last_name", "lastName", "last_name_en"})
  @JsonProperty("last_name_en")
  private String lastNameEn;

  @NotBlank(message = "first_name_kh is required")
  @JsonProperty("first_name_kh")
  private String firstNameKh;

  @NotBlank(message = "last_name_kh is required")
  @JsonProperty("last_name_kh")
  private String lastNameKh;

  @NotBlank(message = "sex is required")
  @JsonAlias("gender")
  private String sex;

  @JsonProperty("date_of_birth")
  private LocalDate dateOfBirth;

  @JsonProperty("national_id")
  private String nationalId;

  private String nationality;

  private String ethnicity;

  @NotBlank(message = "email is required")
  @Email(message = "Invalid email format")
  private String email;

  @JsonProperty("position_id")
  private Long positionId;

  private String position;

  @JsonProperty("office_id")
  private Long officeId;

  @JsonAlias({"office", "office_name"})
  private String department;

  @JsonProperty("education_level")
  @JsonAlias({"education_level", "education_level_name"})
  private String educationLevel;

  @NotNull(message = "hire_date is required")
  @JsonProperty("hire_date")
  private LocalDate hireDate;

  @JsonProperty("contract_type")
  private String contractType;

  @NotBlank(message = "phone is required")
  @Size(max = 100, message = "phone must not exceed 100 characters")
  @Pattern(regexp = "^[0-9+\\-() ]*$", message = "Invalid phone number format")
  private String phone;

  @NotBlank(message = "status is required")
  private String status;

  public String getFirstName() {
    return firstNameEn;
  }

  public String getLastName() {
    return lastNameEn;
  }
}
