package com.norton.backend.controllers;

import com.norton.backend.dto.request.AttendanceScanRequest;
import com.norton.backend.dto.request.CreateAttendanceRequest;
import com.norton.backend.dto.request.UpdateAttendanceStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AttendanceExportResponse;
import com.norton.backend.dto.responses.attendances.AttendanceImportResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.dto.responses.attendances.AttendanceScanSuccessResponse;
import com.norton.backend.dto.responses.attendances.AttendanceStatusResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSummaryResponse;
import com.norton.backend.dto.responses.attendances.CreateAttendanceResponse;
import com.norton.backend.dto.responses.attendances.UpdateAttendanceResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.services.attendance.AttendanceScanService;
import com.norton.backend.services.attendance.AttendanceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AttendanceController.BASE_PATH)
@RequiredArgsConstructor
public class AttendanceController {
  public static final String BASE_PATH = "/api/v1/attendance";

  private final AttendanceService attendanceService;
  private final AttendanceScanService attendanceScanService;

  @GetMapping
  public ResponseEntity<PageResponse<AttendanceResponse>> getAllAttendance(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) String department,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String viewMode) {
    return ResponseEntity.ok(
        attendanceService.getAllAttendance(page, size, search, date, department, status, viewMode));
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

  @GetMapping("/export")
  public ResponseEntity<byte[]> exportAttendance(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) String department,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "xlsx") String format,
      @RequestParam(required = false) String viewMode) {
    if (!"xlsx".equalsIgnoreCase(format)) {
      throw new BadRequestException("Only xlsx export format is supported");
    }

    AttendanceExportResponse file =
        attendanceService.exportAttendance(date, department, status, search, viewMode);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.getContentType()))
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
        .body(file.getContent());
  }

  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AttendanceImportResponse> importAttendance(
      @RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(attendanceService.importAttendance(file));
  }

  @PostMapping("/scan")
  public ResponseEntity<AttendanceScanSuccessResponse> submitAttendanceScan(
      @Valid @RequestBody AttendanceScanRequest request) {
    return ResponseEntity.ok(attendanceScanService.submitScan(request));
  }
}
