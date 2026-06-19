package com.norton.backend.services.dashboard;

import com.norton.backend.dto.responses.dashboard.DashboardResponse;
import com.norton.backend.dto.responses.dashboard.RecentAttendanceDto;
import com.norton.backend.dto.responses.dashboard.RecentAttendanceSessionDto;
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
                .total(120)
                .active(98)
                .onLeave(12)
                .inactive(10)
                .build())
        .attendance(
            DashboardResponse.AttendanceSummary.builder()
                .total(102)
                .approved(95)
                .pending(5)
                .absent(3)
                .build())
        .invitations(
            DashboardResponse.InvitationsSummary.builder().total(15).active(8).completed(7).build())
        .missions(
            DashboardResponse.ApprovalSummary.builder().total(9).approved(5).pending(4).build())
        .leaveRequests(
            DashboardResponse.ApprovalSummary.builder().total(24).approved(18).pending(6).build())
        .recentAttendance(
            List.of(
                RecentAttendanceDto.builder()
                    .id(1L)
                    .officerId(101L)
                    .imageUrl(null)
                    .date("09/06/2026")
                    .checkIn("08:09")
                    .checkOut("17:05")
                    .totalWorkMin(536)
                    .totalLateMin(9)
                    .status("មាន")
                    .firstName("សុខា")
                    .lastName("បុត្រា")
                    .department("ការិយាល័យគ្រប់គ្រង")
                    .officerCode("OF-101")
                    .sessions(
                        List.of(
                            RecentAttendanceSessionDto.builder()
                                .id(1001L)
                                .shiftName("ព្រឹក")
                                .checkIn("08:09")
                                .checkOut("17:05")
                                .status("សម្រេច")
                                .build()))
                    .build()))
        .build();
  }
}
