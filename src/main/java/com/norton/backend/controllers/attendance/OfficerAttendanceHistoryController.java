package com.norton.backend.controllers.attendance;

import com.norton.backend.dto.responses.attendances.OfficerAttendanceDailyDetailResponse;
import com.norton.backend.dto.responses.attendances.OfficerAttendanceMonthlyHistoryResponse;
import com.norton.backend.dto.responses.attendances.OfficerAttendanceTodayScanInfoResponse;
import com.norton.backend.services.attendance.AttendanceService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(OfficerAttendanceHistoryController.BASE_PATH)
@RequiredArgsConstructor
public class OfficerAttendanceHistoryController {

  public static final String BASE_PATH = "/api";

  private final AttendanceService attendanceService;

  /**
   * Get officer attendance monthly history with summary and dates breakdown Example: GET
   * /api/getOfficerAttendanceMonthlyHistory/1/2026-12
   */
  @GetMapping({
    "/getOfficerAttendanceMonthlyHistory/{officerId}/{onMonth}",
    "/v1/getOfficerAttendanceMonthlyHistory/{officerId}/{onMonth}"
  })
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ATTENDANCE_VIEW)")
  public ResponseEntity<OfficerAttendanceMonthlyHistoryResponse> getOfficerAttendanceMonthlyHistory(
      @PathVariable Long officerId, @PathVariable String onMonth) {
    return ResponseEntity.ok(
        attendanceService.getOfficerAttendanceMonthlyHistory(officerId, onMonth));
  }

  /**
   * Get officer attendance daily detail with check-in/out times and timeline Example: GET
   * /api/getOfficerAttendanceDailyDetail/1/2026-12-21
   */
  @GetMapping({
    "/getOfficerAttendanceDailyDetail/{officerId}/{onDate}",
    "/v1/getOfficerAttendanceDailyDetail/{officerId}/{onDate}"
  })
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ATTENDANCE_VIEW)")
  public ResponseEntity<OfficerAttendanceDailyDetailResponse> getOfficerAttendanceDailyDetail(
      @PathVariable Long officerId,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate) {
    return ResponseEntity.ok(attendanceService.getOfficerAttendanceDailyDetail(officerId, onDate));
  }

  /**
   * Get officer attendance for today with working duration and timeline Example: GET
   * /api/getOfficerAttendanceTodayScanInfo/1
   */
  @GetMapping({
    "/getOfficerAttendanceTodayScanInfo/{officerId}",
    "/v1/getOfficerAttendanceTodayScanInfo/{officerId}"
  })
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ATTENDANCE_VIEW)")
  public ResponseEntity<OfficerAttendanceTodayScanInfoResponse> getOfficerAttendanceTodayScanInfo(
      @PathVariable Long officerId) {
    return ResponseEntity.ok(attendanceService.getOfficerAttendanceTodayScanInfo(officerId));
  }
}
