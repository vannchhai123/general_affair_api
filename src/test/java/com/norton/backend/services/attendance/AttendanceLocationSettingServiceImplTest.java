package com.norton.backend.services.attendance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.norton.backend.dto.attendances.AttendanceLocationDto;
import com.norton.backend.dto.attendances.LatLngDto;
import com.norton.backend.dto.request.attendances.CreateAttendanceLocationRequest;
import com.norton.backend.dto.request.attendances.UpdateAttendanceLocationRequest;
import com.norton.backend.dto.responses.attendances.AttendanceLocationSettingsResponse;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.AttendanceLocationModel;
import com.norton.backend.models.AttendanceLocationSettingModel;
import com.norton.backend.models.embeddable.LatLngPoint;
import com.norton.backend.repositories.AttendanceLocationRepository;
import com.norton.backend.repositories.AttendanceLocationSettingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceLocationSettingServiceImplTest {

  @Mock private AttendanceLocationSettingRepository settingRepository;
  @Mock private AttendanceLocationRepository locationRepository;

  @InjectMocks private AttendanceLocationSettingServiceImpl service;

  @Test
  void getSettings_whenExists_returnsMappedResponse() {
    LatLngPoint point = new LatLngPoint(11.577, 104.869);
    AttendanceLocationModel location =
        AttendanceLocationModel.builder().name("Sen Sok Office").boundary(List.of(point)).build();

    AttendanceLocationSettingModel setting =
        AttendanceLocationSettingModel.builder()
            .isGlobalEnabled(true)
            .locations(List.of(location))
            .build();

    when(settingRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(setting));

    AttendanceLocationSettingsResponse response = service.getSettings();

    assertNotNull(response);
    assertTrue(response.getIsGlobalEnabled());
    assertEquals(1, response.getLocations().size());
    assertEquals("Sen Sok Office", response.getLocations().get(0).getName());
    assertEquals(11.577, response.getLocations().get(0).getBoundary().get(0).getLatitude());
  }

  @Test
  void toggleGlobalSetting_updatesAndReturnsSetting() {
    AttendanceLocationSettingModel setting =
        AttendanceLocationSettingModel.builder()
            .isGlobalEnabled(true)
            .locations(new ArrayList<>())
            .build();

    when(settingRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(setting));
    when(settingRepository.save(any(AttendanceLocationSettingModel.class)))
        .thenAnswer(i -> i.getArgument(0));

    AttendanceLocationSettingsResponse response = service.toggleGlobalSetting(false);

    assertNotNull(response);
    assertFalse(response.getIsGlobalEnabled());
  }

  @Test
  void getAllLocations_returnsList() {
    AttendanceLocationModel location =
        AttendanceLocationModel.builder()
            .name("Sen Sok Office")
            .boundary(List.of(new LatLngPoint(11.577, 104.869)))
            .build();

    when(locationRepository.findAllByOrderByIdAsc()).thenReturn(List.of(location));

    List<AttendanceLocationDto> result = service.getAllLocations();

    assertEquals(1, result.size());
    assertEquals("Sen Sok Office", result.get(0).getName());
  }

  @Test
  void getLocationById_whenExists_returnsDto() {
    AttendanceLocationModel location =
        AttendanceLocationModel.builder()
            .name("Sen Sok Office")
            .boundary(List.of(new LatLngPoint(11.577, 104.869)))
            .build();

    when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

    AttendanceLocationDto result = service.getLocationById(1L);

    assertNotNull(result);
    assertEquals("Sen Sok Office", result.getName());
  }

  @Test
  void getLocationById_whenNotFound_throwsException() {
    when(locationRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.getLocationById(99L));
  }

  @Test
  void createLocation_savesAndReturnsDto() {
    AttendanceLocationSettingModel setting =
        AttendanceLocationSettingModel.builder()
            .isGlobalEnabled(true)
            .locations(new ArrayList<>())
            .build();

    when(settingRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(setting));
    when(locationRepository.save(any(AttendanceLocationModel.class)))
        .thenAnswer(i -> i.getArgument(0));

    CreateAttendanceLocationRequest request =
        CreateAttendanceLocationRequest.builder()
            .name("Norton University")
            .boundary(List.of(new LatLngDto(11.588, 104.929)))
            .build();

    AttendanceLocationDto result = service.createLocation(request);

    assertNotNull(result);
    assertEquals("Norton University", result.getName());
    assertEquals(1, result.getBoundary().size());
  }

  @Test
  void updateLocation_whenExists_updatesAndReturnsDto() {
    AttendanceLocationModel location =
        AttendanceLocationModel.builder().name("Old Office").boundary(new ArrayList<>()).build();

    when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
    when(locationRepository.save(any(AttendanceLocationModel.class)))
        .thenAnswer(i -> i.getArgument(0));

    UpdateAttendanceLocationRequest request =
        UpdateAttendanceLocationRequest.builder()
            .name("Updated Office")
            .boundary(List.of(new LatLngDto(11.588, 104.929)))
            .build();

    AttendanceLocationDto result = service.updateLocation(1L, request);

    assertNotNull(result);
    assertEquals("Updated Office", result.getName());
  }

  @Test
  void deleteLocation_whenExists_deletesSuccessfully() {
    AttendanceLocationModel location = AttendanceLocationModel.builder().name("To Delete").build();

    when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
    doNothing().when(locationRepository).delete(location);

    assertDoesNotThrow(() -> service.deleteLocation(1L));
    verify(locationRepository, times(1)).delete(location);
  }
}
