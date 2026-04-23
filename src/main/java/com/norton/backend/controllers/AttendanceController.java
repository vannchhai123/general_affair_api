package com.norton.backend.controllers;

import com.norton.backend.dto.request.AttendanceScanRequest;
import com.norton.backend.dto.request.CreateAttendanceRequest;
import com.norton.backend.dto.request.UpdateAttendanceStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.dto.responses.attendances.AttendanceScanSuccessResponse;
import com.norton.backend.dto.responses.attendances.AttendanceStatusResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSummaryResponse;
import com.norton.backend.dto.responses.attendances.CreateAttendanceResponse;
import com.norton.backend.dto.responses.attendances.UpdateAttendanceResponse;
import com.norton.backend.services.attendance.AttendanceScanService;
import com.norton.backend.services.attendance.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AttendanceController.BASE_PATH)
@RequiredArgsConstructor
public class AttendanceController {
  public static final String BASE_PATH = "/api/v1/attendance";

  private final AttendanceService attendanceService;
  private final AttendanceScanService attendanceScanService;

  @GetMapping
  public ResponseEntity<PageResponse<AttendanceResponse>> getAllAttendance(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(attendanceService.getAllAttendance(page, size));
  }

  @GetMapping("/status")
  public ResponseEntity<AttendanceStatusResponse> getMyAttendanceStatus(
      @RequestParam(required = false) Long officerId) {
    return ResponseEntity.ok(attendanceService.getMyAttendanceStatus(officerId));
  }

  @GetMapping("/summary")
  public ResponseEntity<AttendanceSummaryResponse> getMyAttendanceSummary(
      @RequestParam(required = false) Long officerId) {
    return ResponseEntity.ok(attendanceService.getMyAttendanceSummary(officerId));
  }

  @PostMapping
  public ResponseEntity<CreateAttendanceResponse> createAttendance(
      @Valid @RequestBody CreateAttendanceRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(attendanceService.createAttendance(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UpdateAttendanceResponse> updateAttendanceStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateAttendanceStatusRequest request) {
    return ResponseEntity.ok(attendanceService.updateAttendanceStatus(id, request));
  }

  @PostMapping("/scan")
  public ResponseEntity<AttendanceScanSuccessResponse> submitAttendanceScan(
      @Valid @RequestBody AttendanceScanRequest request) {
    return ResponseEntity.ok(attendanceScanService.submitScan(request));
  }
}
