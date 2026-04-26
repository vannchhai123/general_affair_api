package com.norton.backend.dto.responses.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationSummaryResponse {
  @JsonProperty("departments_total")
  private long departmentsTotal;

  @JsonProperty("departments_active")
  private long departmentsActive;

  @JsonProperty("positions_total")
  private long positionsTotal;

  @JsonProperty("positions_active")
  private long positionsActive;

  @JsonProperty("assigned_officers")
  private long assignedOfficers;
}
