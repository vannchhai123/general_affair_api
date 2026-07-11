package com.norton.backend.services.mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.enums.MeetingStatus;
import com.norton.backend.models.MeetingModel;
import com.norton.backend.repositories.MeetingRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.ShiftAssignmentRepository;
import com.norton.backend.services.invitation.InvitationService;
import com.norton.backend.utils.SecurityUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MobileHomeServiceImplTest {

  @Mock private MeetingRepository meetingRepository;
  @Mock private InvitationService invitationService;
  @Mock private SecurityUtils securityUtils;
  @Mock private OfficerRepository officerRepository;
  @Mock private ShiftAssignmentRepository shiftAssignmentRepository;

  @InjectMocks private MobileHomeServiceImpl mobileHomeService;

  @Test
  void getHomeData_returnsSummaryStatisticsAndRecentMeetingsForCurrentUser() {
    org.springframework.test.util.ReflectionTestUtils.setField(
        mobileHomeService, "scanTimezone", java.time.ZoneId.systemDefault().toString());

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

  @Test
  void getMeetingCalendar_returnsMeetingDatesWithinRequestedMonth() {
    when(securityUtils.getCurrentUserId()).thenReturn(7L);
    when(meetingRepository.findMeetingCountsByAssigneeIdAndMeetingDateBetween(
            7L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)))
        .thenReturn(
            List.of(
                createCalendarProjection(LocalDate.of(2026, 7, 2), 1L),
                createCalendarProjection(LocalDate.of(2026, 7, 5), 3L),
                createCalendarProjection(LocalDate.of(2026, 7, 15), 1L)));

    var response = mobileHomeService.getMeetingCalendar(2026, 7);

    assertNotNull(response);
    assertEquals(2026, response.getYear());
    assertEquals(7, response.getMonth());
    assertEquals(3, response.getDates().size());
    assertEquals("2026-07-02", response.getDates().get(0).getDate());
    assertEquals(3L, response.getDates().get(1).getMeetingCount());
  }

  private MeetingRepository.MeetingCalendarDateProjection createCalendarProjection(
      LocalDate date, Long meetingCount) {
    return new MeetingRepository.MeetingCalendarDateProjection() {
      @Override
      public LocalDate getDate() {
        return date;
      }

      @Override
      public Long getMeetingCount() {
        return meetingCount;
      }
    };
  }

  @Test
  void getMeetingDetail_returnsInvitationResponseFromInvitationService() {
    CreateInvitationResponse meetingDetail =
        CreateInvitationResponse.builder()
            .id(123L)
            .title("Team Planning")
            .description("Planning for next quarter")
            .presidedBy("Director")
            .eventDate("2026-07-20")
            .eventTime("09:30")
            .location("Conference Room A")
            .build();

    when(invitationService.getInvitationById(123L)).thenReturn(meetingDetail);

    CreateInvitationResponse response = mobileHomeService.getMeetingDetail(123L);

    assertNotNull(response);
    assertEquals(123L, response.getId());
    assertEquals("Team Planning", response.getTitle());
    assertEquals("Planning for next quarter", response.getDescription());
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

  @Test
  void getMyShift_returnsAssignedShifts_onWeekday() {
    org.springframework.test.util.ReflectionTestUtils.setField(
        mobileHomeService, "scanTimezone", "Asia/Phnom_Penh");

    when(securityUtils.getCurrentUserId()).thenReturn(7L);

    com.norton.backend.models.DepartmentModel department =
        new com.norton.backend.models.DepartmentModel();
    department.setId(10L);
    department.setName("Finance");

    com.norton.backend.models.PositionModel position =
        new com.norton.backend.models.PositionModel();
    position.setId(20L);
    position.setDepartment(department);

    com.norton.backend.models.OfficerModel officer = new com.norton.backend.models.OfficerModel();
    officer.setId(30L);
    officer.setOfficerCode("OFF-100");
    officer.setFirstNameEn("John");
    officer.setLastNameEn("Doe");
    officer.setPosition(position);
    officer.setOffice(department);

    when(officerRepository.findByUserIdWithPosition(7L)).thenReturn(Optional.of(officer));

    com.norton.backend.models.ShiftModel shift = new com.norton.backend.models.ShiftModel();
    shift.setId(40L);
    shift.setName("Morning Shift");
    shift.setStartTime(LocalTime.of(8, 0));
    shift.setEndTime(LocalTime.of(17, 0));

    com.norton.backend.models.ShiftAssignmentModel assignment =
        new com.norton.backend.models.ShiftAssignmentModel();
    assignment.setId(50L);
    assignment.setShift(shift);
    assignment.setScope(com.norton.backend.enums.ShiftAssignmentScope.DEPARTMENT);
    assignment.setScopeId(10L);
    assignment.setScopeName("Finance");
    assignment.setDayOfWeek(com.norton.backend.enums.ShiftDayOfWeek.MON);

    // Mock findEffectiveAssignments to return a weekday assignment
    when(shiftAssignmentRepository.findEffectiveAssignments(
            anyList(), anyList(), any(), any(LocalDate.class)))
        .thenReturn(List.of(assignment));

    var response = mobileHomeService.getMyShift();

    assertNotNull(response);
    // Since we mocked findEffectiveAssignments to return a matching MON assignment,
    // matchesOfficerScope will be evaluated: scope is DEPARTMENT, scopeId (10L) equals departmentId
    // (10L), which matches!
    // But wait, what day of week does the test run on?
    // LocalDate.now(resolveZoneId()) returns the current local date.
    // If today is a Monday, dayOfWeek is MON, and toShiftDayOfWeek maps it to MON, so it returns
    // assigned=true.
    // Wait, to make the test date-independent, we don't mock LocalDate.now(), but we mocked
    // findEffectiveAssignments to return a MON assignment.
    // In matchesOfficerScope, it only checks scopeId matches departmentId.
    // Since scopeId is 10L, scope is DEPARTMENT, and departmentId is 10L, matchesOfficerScope
    // returns true.
    // So it will be evaluated and returned!
    assertTrue(response.isAssigned());
    assertEquals(1, response.getShifts().size());
    assertEquals("Morning Shift", response.getShifts().get(0).getShift().getName());
  }

  @Test
  void getMyShift_returnsUnassigned_whenOfficerNotFound() {
    when(securityUtils.getCurrentUserId()).thenReturn(1L);
    when(officerRepository.findByUserIdWithPosition(1L)).thenReturn(Optional.empty());

    var response = mobileHomeService.getMyShift();

    assertNotNull(response);
    org.junit.jupiter.api.Assertions.assertFalse(response.isAssigned());
    assertTrue(response.getShifts().isEmpty());
  }
}
