package com.norton.backend.dto.responses.auth;

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
public class AdminMeResponse {

  private String uuid;
  private String username;
  private String fullName;
  private Boolean enabled;

  @JsonProperty("avatarUrl")
  private String avatarUrl;

  private List<AdminMeRoleDto> roles;
  private List<String> permissions;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AdminMeRoleDto {
    private Long id;
    private String code;

    @JsonProperty("name_km")
    private String nameKm;
  }
}
