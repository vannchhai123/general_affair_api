package com.norton.backend.services.attendance;

import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;

public interface AttendanceService {
  PageResponse<AttendanceResponse> getAllAttendance(int page, int size);
}
