package com.norton.backend.dto.responses.superadmin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInvitationResponse {

  private Long id;
  private String email;

  @JsonProperty("fullName")
  private String fullName;

  @JsonProperty("khmerName")
  private String khmerName;

  @JsonProperty("roleName")
  private String roleName;

  private String status;

  @JsonProperty("expiresAt")
  private Instant expiresAt;
}
