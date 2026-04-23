package com.norton.backend.services.attendance;

import com.norton.backend.dto.request.CreateAttendanceRequest;
import com.norton.backend.dto.request.UpdateAttendanceStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.dto.responses.attendances.AttendanceStatusResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSummaryResponse;
import com.norton.backend.dto.responses.attendances.CreateAttendanceResponse;
import com.norton.backend.dto.responses.attendances.UpdateAttendanceResponse;

public interface AttendanceService {
  PageResponse<AttendanceResponse> getAllAttendance(int page, int size);

  AttendanceStatusResponse getMyAttendanceStatus(Long officerId);

  AttendanceSummaryResponse getMyAttendanceSummary(Long officerId);

  CreateAttendanceResponse createAttendance(CreateAttendanceRequest request);

  UpdateAttendanceResponse updateAttendanceStatus(Long id, UpdateAttendanceStatusRequest request);
}
