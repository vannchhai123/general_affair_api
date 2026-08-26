package com.norton.backend.controllers.attendance;

import com.norton.backend.dto.attendances.AttendanceLocationDto;
import com.norton.backend.dto.request.attendances.CreateAttendanceLocationRequest;
import com.norton.backend.dto.request.attendances.ToggleLocationSettingRequest;
import com.norton.backend.dto.request.attendances.UpdateAttendanceLocationRequest;
import com.norton.backend.dto.request.attendances.UpdateAttendanceLocationSettingsRequest;
import com.norton.backend.dto.responses.attendances.AttendanceLocationSettingsResponse;
import com.norton.backend.services.attendance.AttendanceLocationSettingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(AttendanceLocationController.BASE_PATH)
public class AttendanceLocationController {

  public static final String BASE_PATH = "/api/v1/attendance/locations";

  private final AttendanceLocationSettingService locationService;

  @GetMapping
  public ResponseEntity<List<AttendanceLocationDto>> getAllLocations() {
    return ResponseEntity.ok(locationService.getAllLocations());
  }

  @GetMapping("/{id}")
  public ResponseEntity<AttendanceLocationDto> getLocationById(@PathVariable Long id) {
    return ResponseEntity.ok(locationService.getLocationById(id));
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AttendanceLocationDto> createLocation(
      @Valid @RequestBody CreateAttendanceLocationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createLocation(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AttendanceLocationDto> updateLocation(
      @PathVariable Long id, @Valid @RequestBody UpdateAttendanceLocationRequest request) {
    return ResponseEntity.ok(locationService.updateLocation(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> deleteLocation(@PathVariable Long id) {
    locationService.deleteLocation(id);
    return ResponseEntity.ok(
        Map.of("success", true, "message", "Attendance location deleted successfully"));
  }

  @GetMapping("/settings")
  public ResponseEntity<AttendanceLocationSettingsResponse> getSettings() {
    return ResponseEntity.ok(locationService.getSettings());
  }

  @PatchMapping("/settings/toggle")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AttendanceLocationSettingsResponse> toggleGlobalSetting(
      @Valid @RequestBody ToggleLocationSettingRequest request) {
    return ResponseEntity.ok(locationService.toggleGlobalSetting(request.getIsGlobalEnabled()));
  }

  @PutMapping("/settings")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AttendanceLocationSettingsResponse> updateFullSettings(
      @Valid @RequestBody UpdateAttendanceLocationSettingsRequest request) {
    return ResponseEntity.ok(locationService.updateSettings(request));
  }
}
