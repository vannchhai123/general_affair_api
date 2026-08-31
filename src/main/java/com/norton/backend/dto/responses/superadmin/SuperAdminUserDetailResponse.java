package com.norton.backend.dto.responses.superadmin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.enums.UserStatus;
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
public class SuperAdminUserDetailResponse {

  private Long id;
  private String uuid;
  private String username;
  private String email;

  @JsonProperty("full_name")
  private String fullName;

  @JsonProperty("user_status")
  private UserStatus userStatus;

  @JsonProperty("image_url")
  private String imageUrl;

  private RoleSimpleResponse role;
  private List<RoleSimpleResponse> roles;

  @JsonProperty("officer_id")
  private Long officerId;

  @JsonProperty("officer_code")
  private String officerCode;

  @JsonProperty("department_name")
  private String departmentName;

  @JsonProperty("position_name")
  private String positionName;

  private List<String> permissions;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("updated_at")
  private Instant updatedAt;
}
