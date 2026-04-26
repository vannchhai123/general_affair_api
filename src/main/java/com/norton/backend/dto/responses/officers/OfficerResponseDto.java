package com.norton.backend.dto.responses.officers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OfficerResponseDto {
  private Long id;

  @JsonProperty("user_id")
  private Long userId;

  private String uuid;
  private String officerCode;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  private String email;
  private String sex;

  @JsonProperty("image_url")
  private String imageUrl;

  private String position;
  private String department;
  private String phone;
  private String status;
  private String username;
}
