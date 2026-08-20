package com.norton.backend.dto.attendances;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class AttendanceLocationDto {

  private Long id;

  @NotBlank(message = "Location name is required")
  private String name;

  @NotEmpty(message = "Boundary points are required")
  @Valid
  private List<LatLngDto> boundary;
}
