package com.norton.backend.dto.responses.officers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOfficerResponse {

  private Long id;

  @JsonProperty("user_id")
  private Long userId;

  private String officerCode;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  private String sex;
  private String email;
  private String position;
  private String department;
  private String phone;
  private String status;
  private String username;
}
