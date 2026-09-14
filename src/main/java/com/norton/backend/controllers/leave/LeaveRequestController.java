package com.norton.backend.controllers.leave;

import com.norton.backend.dto.request.leave.CreateLeaveRequestRequest;
import com.norton.backend.dto.request.leave.UpdateLeaveRequestRequest;
import com.norton.backend.dto.responses.leave.LeaveRequestResponse;
import com.norton.backend.dto.responses.leave.LeaveTypeResponse;
import com.norton.backend.services.leave.LeaveRequestService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(LeaveRequestController.BASE_URL)
public class LeaveRequestController {

  public static final String BASE_URL = "/api/v1/leave-requests";

  private final LeaveRequestService leaveRequestService;

  @GetMapping
  public ResponseEntity<List<LeaveRequestResponse>> getAllLeaveRequests() {
    List<LeaveRequestResponse> requests = leaveRequestService.getAllLeaveRequests();
    return ResponseEntity.ok(requests);
  }

  @GetMapping("/count/approved")
  public ResponseEntity<Map<String, Object>> getTodayApprovedLeaveCount(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date) {
    LocalDate targetDate = date != null ? date : LocalDate.now();
    long count = leaveRequestService.countTodayApprovedLeaves(targetDate);
    return ResponseEntity.ok(
        Map.of("date", targetDate.toString(), "status", "APPROVED", "count", count));
  }

  @GetMapping("/today/approved")
  public ResponseEntity<List<LeaveRequestResponse>> getTodayApprovedLeaves(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date) {
    LocalDate targetDate = date != null ? date : LocalDate.now();
    return ResponseEntity.ok(leaveRequestService.getTodayApprovedLeaves(targetDate));
  }

  @GetMapping("/types")
  public ResponseEntity<List<LeaveTypeResponse>> getLeaveTypes() {
    List<LeaveTypeResponse> types = leaveRequestService.getLeaveTypes();
    return ResponseEntity.ok(types);
  }

  @PostMapping
  public ResponseEntity<LeaveRequestResponse> createLeaveRequest(
      @RequestBody CreateLeaveRequestRequest request) {
    LeaveRequestResponse created = leaveRequestService.createLeaveRequest(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<LeaveRequestResponse> updateLeaveRequest(
      @PathVariable Long id, @RequestBody UpdateLeaveRequestRequest request) {
    LeaveRequestResponse updated = leaveRequestService.updateLeaveRequest(id, request);
    return ResponseEntity.ok(updated);
  }
}
