package com.norton.backend.dto.responses.superadmin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminStatsResponse {

  @JsonProperty("total_users")
  private long totalUsers;

  @JsonProperty("active_users")
  private long activeUsers;

  @JsonProperty("banned_users")
  private long bannedUsers;

  @JsonProperty("total_officers")
  private long totalOfficers;

  @JsonProperty("total_roles")
  private long totalRoles;

  @JsonProperty("total_permissions")
  private long totalPermissions;

  @JsonProperty("total_departments")
  private long totalDepartments;

  @JsonProperty("total_positions")
  private long totalPositions;
}
