package com.norton.backend.controllers.mobile;

import com.norton.backend.dto.request.leave.CreateLeaveRequestRequest;
import com.norton.backend.dto.responses.leave.LeaveRequestResponse;
import com.norton.backend.dto.responses.leave.LeaveTypeResponse;
import com.norton.backend.services.leave.LeaveRequestService;
import com.norton.backend.utils.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(MobileLeaveRequestController.BASE_URL)
public class MobileLeaveRequestController {

  public static final String BASE_URL = "/api/v1/mobile/leave-requests";

  private final LeaveRequestService leaveRequestService;
  private final SecurityUtils securityUtils;

  @GetMapping("/types")
  public ResponseEntity<List<LeaveTypeResponse>> getLeaveTypes() {
    List<LeaveTypeResponse> types = leaveRequestService.getLeaveTypes();
    return ResponseEntity.ok(types);
  }

  @PostMapping
  public ResponseEntity<LeaveRequestResponse> submitLeaveRequest(
      @RequestBody CreateLeaveRequestRequest request) {
    if (request.getOfficerId() == null) {
      Long currentUserId = securityUtils.getCurrentUserId();
      request.setOfficerId(currentUserId);
    }
    LeaveRequestResponse created = leaveRequestService.createLeaveRequest(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/my-requests")
  public ResponseEntity<List<LeaveRequestResponse>> getMyLeaveRequests(
      @RequestParam(required = false) Long officerId) {
    Long targetOfficerId = officerId != null ? officerId : securityUtils.getCurrentUserId();
    List<LeaveRequestResponse> requests =
        leaveRequestService.getOfficerLeaveRequests(targetOfficerId);
    return ResponseEntity.ok(requests);
  }

  @GetMapping("/{id}")
  public ResponseEntity<LeaveRequestResponse> getLeaveRequestDetail(@PathVariable Long id) {
    LeaveRequestResponse response = leaveRequestService.getLeaveRequestById(id);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<LeaveRequestResponse> cancelLeaveRequest(@PathVariable Long id) {
    LeaveRequestResponse cancelled = leaveRequestService.cancelLeaveRequest(id);
    return ResponseEntity.ok(cancelled);
  }
}
