package com.norton.backend.dto.request.superadmin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserInvitationRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Full name is required")
  @JsonProperty("fullName")
  @JsonAlias({"fullName", "full_name"})
  private String fullName;

  @JsonProperty("khmerName")
  @JsonAlias({"khmerName", "khmer_name", "name_km", "nameKm"})
  private String khmerName;

  @NotNull(message = "Role ID is required")
  @JsonProperty("roleId")
  @JsonAlias({"roleId", "role_id"})
  private Long roleId;
}
