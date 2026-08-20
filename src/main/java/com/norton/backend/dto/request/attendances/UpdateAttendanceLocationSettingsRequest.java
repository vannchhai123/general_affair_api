package com.norton.backend.dto.request.attendances;

import com.norton.backend.dto.attendances.AttendanceLocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
public class UpdateAttendanceLocationSettingsRequest {

  @NotNull(message = "isGlobalEnabled flag is required")
  private Boolean isGlobalEnabled;

  @Valid private List<AttendanceLocationDto> locations;
}
