package com.norton.backend.controllers;

import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.services.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AttendanceController.BASE_PATH)
@RequiredArgsConstructor
public class AttendanceController {
  public static final String BASE_PATH = "/api/v1/attendance";

  private final AttendanceService attendanceService;

  @GetMapping
  public ResponseEntity<PageResponse<AttendanceResponse>> getAllAttendance(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(attendanceService.getAllAttendance(page, size));
  }
}
