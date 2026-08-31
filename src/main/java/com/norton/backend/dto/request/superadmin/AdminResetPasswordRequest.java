package com.norton.backend.dto.request.superadmin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResetPasswordRequest {

  @NotBlank(message = "New password is required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  @JsonProperty("newPassword")
  @JsonAlias({"newPassword", "new_password", "password"})
  private String newPassword;
}
