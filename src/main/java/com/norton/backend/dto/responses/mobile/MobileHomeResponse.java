package com.norton.backend.dto.responses.mobile;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileHomeResponse {

  private MobileHomeSummaryDto summary;
  private MobileHomeStatisticsDto statistics;
  private List<RecentMeetingDto> recentMeetings;
}
