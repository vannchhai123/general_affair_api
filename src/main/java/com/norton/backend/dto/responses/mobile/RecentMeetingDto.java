package com.norton.backend.dto.responses.mobile;

import com.norton.backend.enums.MeetingStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentMeetingDto {

  private Long id;
  private String title;
  private LocalDate meetingDate;
  private LocalTime meetingTime;
  private MeetingStatus status;
}
