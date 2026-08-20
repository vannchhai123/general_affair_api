package com.norton.backend.dto.responses.attendances;

import com.norton.backend.dto.attendances.AttendanceLocationDto;
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
public class AttendanceLocationSettingsResponse {

  private Long id;
  private Boolean isGlobalEnabled;
  private List<AttendanceLocationDto> locations;
}
