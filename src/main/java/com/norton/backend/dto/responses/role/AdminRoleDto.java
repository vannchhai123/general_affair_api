package com.norton.backend.dto.responses.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRoleDto {

  private Long id;
  private String code;
  private String name;

  @JsonProperty("name_km")
  private String nameKm;

  private String description;

  private List<String> permissions;
}
