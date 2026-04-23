package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOfficerRequest {

  @NotBlank(message = "officerCode is required")
  @JsonProperty("officerCode")
  private String officerCode;

  @NotBlank(message = "first_name is required")
  @JsonProperty("first_name")
  private String firstName;

  @NotBlank(message = "last_name is required")
  @JsonProperty("last_name")
  private String lastName;

  @NotBlank(message = "sex is required")
  private String sex;

  @NotBlank(message = "email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "position is required")
  private String position;

  @NotBlank(message = "department is required")
  private String department;

  @NotBlank(message = "phone is required")
  @Size(max = 100, message = "phone must not exceed 100 characters")
  @Pattern(regexp = "^[0-9+\\-() ]*$", message = "Invalid phone number format")
  private String phone;

  @NotBlank(message = "status is required")
  private String status;
}
