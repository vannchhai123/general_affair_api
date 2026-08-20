package com.norton.backend.dto.attendances;

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
public class LatLngDto {

  @NotNull(message = "Latitude is required")
  private Double latitude;

  @NotNull(message = "Longitude is required")
  private Double longitude;
}
