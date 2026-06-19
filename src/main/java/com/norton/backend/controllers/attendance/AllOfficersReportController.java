package com.norton.backend.controllers.attendance;

import com.norton.backend.dto.responses.attendances.AllOfficersReportResponse;
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
@RequestMapping(AllOfficersReportController.BASE_PATH)
@RequiredArgsConstructor
public class AllOfficersReportController {
  public static final String BASE_PATH = "/api";

  private final AttendanceService attendanceService;

  @GetMapping({
    "/getAllOfficersReport/{onOffice}/{onTodayDate}",
    "/v1/getAllOfficersReport/{onOffice}/{onTodayDate}"
  })
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ATTENDANCE_VIEW)")
  public ResponseEntity<AllOfficersReportResponse> getAllOfficersReport(
      @PathVariable String onOffice,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onTodayDate) {
    return ResponseEntity.ok(attendanceService.getAllOfficersReport(onOffice, onTodayDate));
  }

  @GetMapping({
    "/getOfficerReport/{officerId}/{onDate}",
    "/v1/getOfficerReport/{officerId}/{onDate}"
  })
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ATTENDANCE_VIEW)")
  public ResponseEntity<com.norton.backend.dto.responses.attendances.OfficerReportResponse>
      getOfficerReport(
          @PathVariable Long officerId,
          @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate) {
    return ResponseEntity.ok(attendanceService.getOfficerReport(officerId, onDate));
  }
}
