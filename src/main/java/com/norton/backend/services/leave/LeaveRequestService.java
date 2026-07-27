package com.norton.backend.services.leave;

import com.norton.backend.dto.request.leave.CreateLeaveRequestRequest;
import com.norton.backend.dto.request.leave.UpdateLeaveRequestRequest;
import com.norton.backend.dto.responses.leave.LeaveRequestResponse;
import com.norton.backend.dto.responses.leave.LeaveTypeResponse;
import java.util.List;

public interface LeaveRequestService {
  List<LeaveRequestResponse> getAllLeaveRequests();

  List<LeaveRequestResponse> getOfficerLeaveRequests(Long officerId);

  LeaveRequestResponse getLeaveRequestById(Long id);

  LeaveRequestResponse createLeaveRequest(CreateLeaveRequestRequest request);

  LeaveRequestResponse updateLeaveRequest(Long id, UpdateLeaveRequestRequest request);

  LeaveRequestResponse cancelLeaveRequest(Long id);

  List<LeaveTypeResponse> getLeaveTypes();
}
