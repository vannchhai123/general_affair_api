package com.norton.backend.dto.request.superadmin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.norton.backend.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {

  @NotNull(message = "Status is required")
  @JsonProperty("status")
  @JsonAlias({"status", "user_status", "userStatus"})
  private UserStatus status;
}
