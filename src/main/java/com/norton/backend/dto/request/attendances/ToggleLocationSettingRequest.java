package com.norton.backend.dto.request.attendances;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToggleLocationSettingRequest {

  @NotNull(message = "isGlobalEnabled flag is required")
  private Boolean isGlobalEnabled;
}
