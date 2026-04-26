package com.norton.backend.services.dashboard;

import com.norton.backend.dto.responses.dashboard.DashboardResponse;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private final OfficerRepository officerRepository;
  private final AttendanceRepository attendanceRepository;

  @Override
  @Transactional(readOnly = true)
  public DashboardResponse getDashboard() {
    long officersTotal = officerRepository.count();
    long officersActive = officerRepository.countByStatus(OfficerStatus.ACTIVE);
    long officersOnLeave = officerRepository.countByStatus(OfficerStatus.ON_LEAVE);
    long officersInactive = officerRepository.countByStatus(OfficerStatus.INACTIVE);

    long attendanceTotal = attendanceRepository.count();
    long attendanceApproved = attendanceRepository.countByStatusCodeIgnoreCase("APPROVED");
    long attendanceAbsent = attendanceRepository.countByStatusCodeIgnoreCase("ABSENT");
    long attendancePending = Math.max(attendanceTotal - attendanceApproved, 0);

    return DashboardResponse.builder()
        .officers(
            DashboardResponse.OfficersSummary.builder()
                .total(officersTotal)
                .active(officersActive)
                .onLeave(officersOnLeave)
                .inactive(officersInactive)
                .build())
        .attendance(
            DashboardResponse.AttendanceSummary.builder()
                .total(attendanceTotal)
                .approved(attendanceApproved)
                .pending(attendancePending)
                .absent(attendanceAbsent)
                .build())
        .invitations(
            DashboardResponse.InvitationsSummary.builder().total(0).active(0).completed(0).build())
        .missions(
            DashboardResponse.ApprovalSummary.builder().total(0).approved(0).pending(0).build())
        .leaveRequests(
            DashboardResponse.ApprovalSummary.builder().total(0).approved(0).pending(0).build())
        .recentAttendance(List.of())
        .build();
  }
}
