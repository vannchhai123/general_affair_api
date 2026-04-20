package com.norton.backend.dto.responses.officers;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerStatsResponse {
  private long totalElements;
  private long activeCount;
  private long inactiveCount;
  private long onLeaveCount;
}
