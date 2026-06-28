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
public class MobileMeetingCalendarResponse {

  private int year;
  private int month;
  private List<MeetingCalendarDateDto> dates;
}
