package com.norton.backend.dto.responses.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCalendarDateDto {

  private String date;
  private Long meetingCount;
}
