package com.norton.backend.controllers.mobile;

import com.norton.backend.dto.request.leave.CreateLeaveRequestRequest;
import com.norton.backend.dto.responses.leave.LeaveRequestResponse;
import com.norton.backend.dto.responses.leave.LeaveTypeResponse;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.OfficerRepository;
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
  private final OfficerRepository officerRepository;

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
      OfficerModel officer = officerRepository.findByUserId(currentUserId).orElse(null);
      request.setOfficerId(officer != null ? officer.getId() : currentUserId);
    }
    LeaveRequestResponse created = leaveRequestService.createLeaveRequest(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/my-requests")
  public ResponseEntity<List<LeaveRequestResponse>> getMyLeaveRequests(
      @RequestParam(required = false) Long officerId) {
    Long targetId = officerId;
    if (targetId == null) {
      Long currentUserId = securityUtils.getCurrentUserId();
      OfficerModel officer = officerRepository.findByUserId(currentUserId).orElse(null);
      targetId = officer != null ? officer.getId() : currentUserId;
    }
    List<LeaveRequestResponse> requests = leaveRequestService.getOfficerLeaveRequests(targetId);
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
