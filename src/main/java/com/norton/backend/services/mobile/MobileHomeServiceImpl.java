package com.norton.backend.services.mobile;

import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.mobile.MeetingCalendarDateDto;
import com.norton.backend.dto.responses.mobile.MobileHomeResponse;
import com.norton.backend.dto.responses.mobile.MobileHomeStatisticsDto;
import com.norton.backend.dto.responses.mobile.MobileHomeSummaryDto;
import com.norton.backend.dto.responses.mobile.MobileMeetingCalendarResponse;
import com.norton.backend.dto.responses.mobile.MobileShiftResponseDto;
import com.norton.backend.dto.responses.mobile.RecentMeetingDto;
import com.norton.backend.enums.MeetingStatus;
import com.norton.backend.enums.ShiftAssignmentScope;
import com.norton.backend.enums.ShiftDayOfWeek;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.ShiftAssignmentModel;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.repositories.MeetingRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.ShiftAssignmentRepository;
import com.norton.backend.services.invitation.InvitationService;
import com.norton.backend.utils.SecurityUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MobileHomeServiceImpl implements MobileHomeService {

  private final MeetingRepository meetingRepository;
  private final InvitationService invitationService;
  private final SecurityUtils securityUtils;
  private final OfficerRepository officerRepository;
  private final ShiftAssignmentRepository shiftAssignmentRepository;

  @org.springframework.beans.factory.annotation.Value("${attendance.scan.timezone:Asia/Phnom_Penh}")
  private String scanTimezone;

  @Override
  @Transactional(readOnly = true)
  public MobileHomeResponse getHomeData() {
    Long currentUserId = securityUtils.getCurrentUserId();
    LocalDate today = LocalDate.now(resolveZoneId());

    long totalMeetings = meetingRepository.countByAssigneeId(currentUserId);
    long todayMeetings = meetingRepository.countByAssigneeIdAndMeetingDate(currentUserId, today);

    MobileHomeStatisticsDto statistics =
        MobileHomeStatisticsDto.builder()
            .completed(
                meetingRepository.countByAssigneeIdAndStatus(
                    currentUserId, MeetingStatus.COMPLETED))
            .pending(
                meetingRepository.countByAssigneeIdAndStatus(currentUserId, MeetingStatus.PENDING))
            .postponed(
                meetingRepository.countByAssigneeIdAndStatus(
                    currentUserId, MeetingStatus.POSTPONED))
            .cancelled(
                meetingRepository.countByAssigneeIdAndStatus(
                    currentUserId, MeetingStatus.CANCELLED))
            .build();

    List<RecentMeetingDto> recentMeetings =
        meetingRepository
            .findTop5ByAssigneeIdOrderByMeetingDateDescMeetingTimeDescIdDesc(
                currentUserId, Pageable.ofSize(5))
            .stream()
            .map(
                meeting ->
                    RecentMeetingDto.builder()
                        .id(meeting.getId())
                        .title(meeting.getTitle())
                        .meetingDate(meeting.getMeetingDate())
                        .meetingTime(meeting.getMeetingTime())
                        .status(meeting.getStatus())
                        .build())
            .toList();

    return MobileHomeResponse.builder()
        .summary(
            MobileHomeSummaryDto.builder()
                .totalMeetings(totalMeetings)
                .todayMeetings(todayMeetings)
                .build())
        .statistics(statistics)
        .recentMeetings(recentMeetings)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public CreateInvitationResponse getMeetingDetail(Long id) {
    return invitationService.getInvitationById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public MobileMeetingCalendarResponse getMeetingCalendar(int year, int month) {
    Long currentUserId = securityUtils.getCurrentUserId();
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

    List<MeetingCalendarDateDto> dates =
        meetingRepository
            .findMeetingCountsByAssigneeIdAndMeetingDateBetween(currentUserId, startDate, endDate)
            .stream()
            .map(
                projection ->
                    MeetingCalendarDateDto.builder()
                        .date(projection.getDate().toString())
                        .meetingCount(projection.getMeetingCount())
                        .build())
            .toList();

    return MobileMeetingCalendarResponse.builder().year(year).month(month).dates(dates).build();
  }

  @Override
  @Transactional(readOnly = true)
  public MobileShiftResponseDto getMyShift() {
    Long currentUserId = securityUtils.getCurrentUserId();
    Optional<OfficerModel> officerOpt = officerRepository.findByUserIdWithPosition(currentUserId);
    if (officerOpt.isEmpty()) {
      return MobileShiftResponseDto.builder().assigned(false).shifts(new ArrayList<>()).build();
    }
    OfficerModel officer = officerOpt.get();

    LocalDate today = LocalDate.now(resolveZoneId());

    Long positionId = officer.getPosition() != null ? officer.getPosition().getId() : null;
    Long departmentId =
        (officer.getPosition() != null && officer.getPosition().getDepartment() != null)
            ? officer.getPosition().getDepartment().getId()
            : null;

    List<Long> scopeIds = new ArrayList<>();
    scopeIds.add(officer.getId());
    if (positionId != null) {
      scopeIds.add(positionId);
    }
    if (departmentId != null) {
      scopeIds.add(departmentId);
    }

    ShiftDayOfWeek dayOfWeek = toShiftDayOfWeek(today.getDayOfWeek());

    List<ShiftAssignmentModel> assignments =
        shiftAssignmentRepository.findEffectiveAssignments(
            List.of(
                ShiftAssignmentScope.EMPLOYEE,
                ShiftAssignmentScope.POSITION,
                ShiftAssignmentScope.DEPARTMENT),
            scopeIds,
            dayOfWeek,
            today);

    List<ShiftAssignmentModel> activeAssignments =
        assignments.stream()
            .filter(
                assignment -> matchesOfficerScope(assignment, officer, positionId, departmentId))
            .toList();

    if (activeAssignments.isEmpty()) {
      return MobileShiftResponseDto.builder().assigned(false).shifts(new ArrayList<>()).build();
    }

    int minPriority = activeAssignments.stream().mapToInt(this::scopePriority).min().orElse(2);

    List<ShiftAssignmentModel> resolvedAssignments =
        activeAssignments.stream()
            .filter(assignment -> scopePriority(assignment) == minPriority)
            .toList();

    List<MobileShiftResponseDto.ShiftItem> shiftItems =
        resolvedAssignments.stream()
            .map(
                assignment -> {
                  ShiftModel shift = assignment.getShift();
                  MobileShiftResponseDto.ShiftDetails shiftDetails = null;
                  if (shift != null) {
                    shiftDetails =
                        MobileShiftResponseDto.ShiftDetails.builder()
                            .id(shift.getId())
                            .name(shift.getName())
                            .code(shift.getCode())
                            .startTime(shift.getStartTime())
                            .endTime(shift.getEndTime())
                            .graceMinutes(shift.getGraceMinutes())
                            .checkInOpenBeforeMinutes(shift.getCheckInOpenBeforeMinutes())
                            .checkOutCloseAfterMinutes(shift.getCheckOutCloseAfterMinutes())
                            .crossMidnight(shift.getCrossMidnight())
                            .description(shift.getDescription())
                            .build();
                  }

                  MobileShiftResponseDto.AssignmentDetails assignmentDetails =
                      MobileShiftResponseDto.AssignmentDetails.builder()
                          .scope(assignment.getScope().name())
                          .scopeId(assignment.getScopeId())
                          .scopeName(assignment.getScopeName())
                          .dayOfWeek(assignment.getDayOfWeek().name())
                          .effectiveFrom(assignment.getEffectiveFrom())
                          .effectiveTo(assignment.getEffectiveTo())
                          .build();

                  return MobileShiftResponseDto.ShiftItem.builder()
                      .shift(shiftDetails)
                      .assignment(assignmentDetails)
                      .build();
                })
            .toList();

    return MobileShiftResponseDto.builder().assigned(true).shifts(shiftItems).build();
  }

  private ShiftDayOfWeek toShiftDayOfWeek(java.time.DayOfWeek dayOfWeek) {
    return switch (dayOfWeek) {
      case MONDAY -> ShiftDayOfWeek.MON;
      case TUESDAY -> ShiftDayOfWeek.TUE;
      case WEDNESDAY -> ShiftDayOfWeek.WED;
      case THURSDAY -> ShiftDayOfWeek.THU;
      case FRIDAY -> ShiftDayOfWeek.FRI;
      case SATURDAY -> ShiftDayOfWeek.SAT;
      case SUNDAY -> ShiftDayOfWeek.SUN;
    };
  }

  private boolean matchesOfficerScope(
      ShiftAssignmentModel assignment, OfficerModel officer, Long positionId, Long departmentId) {
    return switch (assignment.getScope()) {
      case EMPLOYEE -> assignment.getScopeId().equals(officer.getId());
      case POSITION -> assignment.getScopeId().equals(positionId);
      case DEPARTMENT -> assignment.getScopeId().equals(departmentId);
    };
  }

  private int scopePriority(ShiftAssignmentModel assignment) {
    return switch (assignment.getScope()) {
      case EMPLOYEE -> 0;
      case POSITION -> 1;
      case DEPARTMENT -> 2;
    };
  }

  private java.time.ZoneId resolveZoneId() {
    try {
      return java.time.ZoneId.of(scanTimezone);
    } catch (Exception ex) {
      return java.time.ZoneId.of("Asia/Phnom_Penh");
    }
  }
}
