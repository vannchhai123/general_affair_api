package com.norton.backend.dto.responses.superadmin;

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
public class UserAccessDetailResponse {

  @JsonProperty("officerId")
  private Long officerId;

  @JsonProperty("userId")
  private Long userId;

  @JsonProperty("fullName")
  private String fullName;

  @JsonProperty("assignedRoles")
  private List<AssignedRoleDetailDto> assignedRoles;

  @JsonProperty("directPermissions")
  private List<String> directPermissions;

  @JsonProperty("effectivePermissions")
  private List<String> effectivePermissions;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AssignedRoleDetailDto {
    private Long id;
    private String code;
    private String name;

    @JsonProperty("name_km")
    private String nameKm;

    private List<String> permissions;
  }
}
