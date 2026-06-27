package com.norton.backend.services.mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.norton.backend.enums.MeetingStatus;
import com.norton.backend.models.MeetingModel;
import com.norton.backend.repositories.MeetingRepository;
import com.norton.backend.utils.SecurityUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MobileHomeServiceImplTest {

  @Mock private MeetingRepository meetingRepository;
  @Mock private SecurityUtils securityUtils;

  @InjectMocks private MobileHomeServiceImpl mobileHomeService;

  @Test
  void getHomeData_returnsSummaryStatisticsAndRecentMeetingsForCurrentUser() {
    when(securityUtils.getCurrentUserId()).thenReturn(7L);
    when(meetingRepository.countByAssigneeId(7L)).thenReturn(12L);
    when(meetingRepository.countByAssigneeIdAndMeetingDate(7L, LocalDate.now())).thenReturn(5L);
    when(meetingRepository.countByAssigneeIdAndStatus(7L, MeetingStatus.COMPLETED)).thenReturn(5L);
    when(meetingRepository.countByAssigneeIdAndStatus(7L, MeetingStatus.PENDING)).thenReturn(3L);
    when(meetingRepository.countByAssigneeIdAndStatus(7L, MeetingStatus.POSTPONED)).thenReturn(2L);
    when(meetingRepository.countByAssigneeIdAndStatus(7L, MeetingStatus.CANCELLED)).thenReturn(2L);
    when(meetingRepository.findTop5ByAssigneeIdOrderByMeetingDateDescMeetingTimeDescIdDesc(
            7L, Pageable.ofSize(5)))
        .thenReturn(
            List.of(
                createMeeting(
                    101L,
                    "Monthly Strategy Meeting",
                    LocalDate.of(2026, 7, 12),
                    LocalTime.of(15, 30),
                    MeetingStatus.COMPLETED),
                createMeeting(
                    102L,
                    "Budget Planning",
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 0),
                    MeetingStatus.PENDING)));

    var response = mobileHomeService.getHomeData();

    assertNotNull(response);
    assertEquals(12L, response.getSummary().getTotalMeetings());
    assertEquals(5L, response.getSummary().getTodayMeetings());
    assertEquals(5L, response.getStatistics().getCompleted());
    assertEquals(3L, response.getStatistics().getPending());
    assertEquals(2L, response.getStatistics().getPostponed());
    assertEquals(2L, response.getStatistics().getCancelled());
    assertEquals(2, response.getRecentMeetings().size());
    assertEquals(101L, response.getRecentMeetings().get(0).getId());
    assertEquals("Monthly Strategy Meeting", response.getRecentMeetings().get(0).getTitle());
  }

  private MeetingModel createMeeting(
      Long id, String title, LocalDate date, LocalTime time, MeetingStatus status) {
    MeetingModel meeting = new MeetingModel();
    meeting.setId(id);
    meeting.setTitle(title);
    meeting.setMeetingDate(date);
    meeting.setMeetingTime(time);
    meeting.setStatus(status);
    meeting.setAssigneeId(7L);
    return meeting;
  }
}
