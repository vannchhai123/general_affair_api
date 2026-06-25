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
    "/getAllOfficersAttendanceReport/{onDate}/admin/{adminOfficerId}",
    "/v1/getAllOfficersAttendanceReport/{onDate}/admin/{adminOfficerId}"
  })
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ATTENDANCE_VIEW)")
  public ResponseEntity<AllOfficersReportResponse> getAllOfficersAttendanceReport(
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate,
      @PathVariable Long adminOfficerId) {
    return ResponseEntity.ok(
        attendanceService.getAllOfficersAttendanceReport(onDate, adminOfficerId));
  }

  @GetMapping({
    "/getOfficerReport/{officerIdentifier}/{onDate}",
    "/v1/getOfficerReport/{officerIdentifier}/{onDate}"
  })
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ATTENDANCE_VIEW)")
  public ResponseEntity<com.norton.backend.dto.responses.attendances.OfficerReportResponse>
      getOfficerReport(
          @PathVariable String officerIdentifier,
          @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate) {
    if (officerIdentifier == null || officerIdentifier.isBlank()) {
      throw new IllegalArgumentException("Officer identifier is required");
    }
    if (officerIdentifier.matches("\\d+")) {
      return ResponseEntity.ok(
          attendanceService.getOfficerReport(Long.valueOf(officerIdentifier), onDate));
    }
    return ResponseEntity.ok(attendanceService.getOfficerReport(officerIdentifier, onDate));
  }

  @GetMapping({
    "/getOfficersReport/{officerUuid}/{onDate}",
    "/v1/getOfficersReport/{officerUuid}/{onDate}"
  })
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).ATTENDANCE_VIEW)")
  public ResponseEntity<com.norton.backend.dto.responses.attendances.OfficerReportResponse>
      getOfficerReportByUuid(
          @PathVariable String officerUuid,
          @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate) {
    return ResponseEntity.ok(attendanceService.getOfficerReport(officerUuid, onDate));
  }
}
