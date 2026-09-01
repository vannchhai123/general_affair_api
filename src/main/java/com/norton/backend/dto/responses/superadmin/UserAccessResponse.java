package com.norton.backend.dto.responses.superadmin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessResponse {

  private boolean success;
  private String message;
  private UserAccessData data;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserAccessData {
    @JsonProperty("officerId")
    private Long officerId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("assignedRoles")
    private List<RoleAccessDto> assignedRoles;

    @JsonProperty("directPermissions")
    private List<String> directPermissions;

    @JsonProperty("effectivePermissions")
    private List<String> effectivePermissions;

    @JsonProperty("updatedAt")
    private Instant updatedAt;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RoleAccessDto {
    private Long id;
    private String code;
    private String name;

    @JsonProperty("name_km")
    private String nameKm;
  }
}
