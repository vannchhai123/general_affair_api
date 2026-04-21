package com.norton.backend.services.attendance;

import com.norton.backend.dto.request.AttendanceScanRequest;
import com.norton.backend.dto.responses.attendances.AttendanceScanSuccessResponse;

public interface AttendanceScanService {
  AttendanceScanSuccessResponse submitScan(AttendanceScanRequest request);
}
