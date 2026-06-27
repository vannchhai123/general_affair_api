package com.norton.backend.dto.responses.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileHomeStatisticsDto {

  private long completed;
  private long pending;
  private long postponed;
  private long cancelled;
}
