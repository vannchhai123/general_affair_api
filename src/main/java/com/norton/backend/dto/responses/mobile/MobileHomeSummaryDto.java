package com.norton.backend.dto.responses.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileHomeSummaryDto {

  private long totalMeetings;
  private long todayMeetings;
}
