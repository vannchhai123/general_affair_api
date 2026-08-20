package com.norton.backend.services.attendance;

import com.norton.backend.dto.attendances.AttendanceLocationDto;
import com.norton.backend.dto.request.attendances.CreateAttendanceLocationRequest;
import com.norton.backend.dto.request.attendances.UpdateAttendanceLocationRequest;
import com.norton.backend.dto.request.attendances.UpdateAttendanceLocationSettingsRequest;
import com.norton.backend.dto.responses.attendances.AttendanceLocationSettingsResponse;
import java.util.List;

public interface AttendanceLocationSettingService {

  AttendanceLocationSettingsResponse getSettings();

  AttendanceLocationSettingsResponse updateSettings(
      UpdateAttendanceLocationSettingsRequest request);

  AttendanceLocationSettingsResponse toggleGlobalSetting(Boolean isGlobalEnabled);

  List<AttendanceLocationDto> getAllLocations();

  AttendanceLocationDto getLocationById(Long id);

  AttendanceLocationDto createLocation(CreateAttendanceLocationRequest request);

  AttendanceLocationDto updateLocation(Long id, UpdateAttendanceLocationRequest request);

  void deleteLocation(Long id);
}
