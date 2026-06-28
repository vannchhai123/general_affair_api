package com.norton.backend.services.mobile;

import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.mobile.MeetingCalendarDateDto;
import com.norton.backend.dto.responses.mobile.MobileHomeResponse;
import com.norton.backend.dto.responses.mobile.MobileHomeStatisticsDto;
import com.norton.backend.dto.responses.mobile.MobileHomeSummaryDto;
import com.norton.backend.dto.responses.mobile.MobileMeetingCalendarResponse;
import com.norton.backend.dto.responses.mobile.RecentMeetingDto;
import com.norton.backend.enums.MeetingStatus;
import com.norton.backend.repositories.MeetingRepository;
import com.norton.backend.services.invitation.InvitationService;
import com.norton.backend.utils.SecurityUtils;
import java.time.LocalDate;
import java.util.List;
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

  @Override
  @Transactional(readOnly = true)
  public MobileHomeResponse getHomeData() {
    Long currentUserId = securityUtils.getCurrentUserId();
    LocalDate today = LocalDate.now();

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
}
