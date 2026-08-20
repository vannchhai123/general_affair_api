package com.norton.backend.services.attendance;

import com.norton.backend.dto.attendances.AttendanceLocationDto;
import com.norton.backend.dto.attendances.LatLngDto;
import com.norton.backend.dto.request.attendances.CreateAttendanceLocationRequest;
import com.norton.backend.dto.request.attendances.UpdateAttendanceLocationRequest;
import com.norton.backend.dto.request.attendances.UpdateAttendanceLocationSettingsRequest;
import com.norton.backend.dto.responses.attendances.AttendanceLocationSettingsResponse;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.AttendanceLocationModel;
import com.norton.backend.models.AttendanceLocationSettingModel;
import com.norton.backend.models.embeddable.LatLngPoint;
import com.norton.backend.repositories.AttendanceLocationRepository;
import com.norton.backend.repositories.AttendanceLocationSettingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceLocationSettingServiceImpl implements AttendanceLocationSettingService {

  private final AttendanceLocationSettingRepository settingRepository;
  private final AttendanceLocationRepository locationRepository;

  private AttendanceLocationSettingModel getOrCreateSetting() {
    return settingRepository
        .findFirstByOrderByIdAsc()
        .orElseGet(
            () ->
                settingRepository.save(
                    AttendanceLocationSettingModel.builder()
                        .isGlobalEnabled(true)
                        .locations(new ArrayList<>())
                        .build()));
  }

  @Override
  @Transactional(readOnly = true)
  public AttendanceLocationSettingsResponse getSettings() {
    AttendanceLocationSettingModel settings = getOrCreateSetting();
    return mapToResponse(settings);
  }

  @Override
  @Transactional
  public AttendanceLocationSettingsResponse toggleGlobalSetting(Boolean isGlobalEnabled) {
    AttendanceLocationSettingModel settings = getOrCreateSetting();
    settings.setIsGlobalEnabled(isGlobalEnabled);
    AttendanceLocationSettingModel saved = settingRepository.save(settings);
    return mapToResponse(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AttendanceLocationDto> getAllLocations() {
    return locationRepository.findAllByOrderByIdAsc().stream()
        .map(this::mapLocationToDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public AttendanceLocationDto getLocationById(Long id) {
    AttendanceLocationModel location =
        locationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AttendanceLocation", "id", id));
    return mapLocationToDto(location);
  }

  @Override
  @Transactional
  public AttendanceLocationDto createLocation(CreateAttendanceLocationRequest request) {
    AttendanceLocationSettingModel setting = getOrCreateSetting();

    List<LatLngPoint> boundaryPoints =
        request.getBoundary() != null
            ? request.getBoundary().stream()
                .map(
                    p ->
                        LatLngPoint.builder()
                            .latitude(p.getLatitude())
                            .longitude(p.getLongitude())
                            .build())
                .collect(Collectors.toList())
            : new ArrayList<>();

    AttendanceLocationModel location =
        AttendanceLocationModel.builder()
            .name(request.getName().trim())
            .setting(setting)
            .boundary(boundaryPoints)
            .build();

    AttendanceLocationModel saved = locationRepository.save(location);
    return mapLocationToDto(saved);
  }

  @Override
  @Transactional
  public AttendanceLocationDto updateLocation(Long id, UpdateAttendanceLocationRequest request) {
    AttendanceLocationModel location =
        locationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AttendanceLocation", "id", id));

    location.setName(request.getName().trim());

    List<LatLngPoint> boundaryPoints =
        request.getBoundary() != null
            ? request.getBoundary().stream()
                .map(
                    p ->
                        LatLngPoint.builder()
                            .latitude(p.getLatitude())
                            .longitude(p.getLongitude())
                            .build())
                .collect(Collectors.toList())
            : new ArrayList<>();

    location.setBoundary(boundaryPoints);
    AttendanceLocationModel updated = locationRepository.save(location);
    return mapLocationToDto(updated);
  }

  @Override
  @Transactional
  public void deleteLocation(Long id) {
    AttendanceLocationModel location =
        locationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AttendanceLocation", "id", id));
    locationRepository.delete(location);
  }

  @Override
  @Transactional
  public AttendanceLocationSettingsResponse updateSettings(
      UpdateAttendanceLocationSettingsRequest request) {
    AttendanceLocationSettingModel settings = getOrCreateSetting();
    settings.setIsGlobalEnabled(request.getIsGlobalEnabled());
    settings.getLocations().clear();

    if (request.getLocations() != null) {
      for (AttendanceLocationDto locDto : request.getLocations()) {
        List<LatLngPoint> boundaryPoints =
            locDto.getBoundary() != null
                ? locDto.getBoundary().stream()
                    .map(
                        p ->
                            LatLngPoint.builder()
                                .latitude(p.getLatitude())
                                .longitude(p.getLongitude())
                                .build())
                    .collect(Collectors.toList())
                : new ArrayList<>();

        AttendanceLocationModel locationModel =
            AttendanceLocationModel.builder()
                .name(locDto.getName().trim())
                .setting(settings)
                .boundary(boundaryPoints)
                .build();

        settings.getLocations().add(locationModel);
      }
    }

    AttendanceLocationSettingModel saved = settingRepository.save(settings);
    return mapToResponse(saved);
  }

  private AttendanceLocationSettingsResponse mapToResponse(AttendanceLocationSettingModel model) {
    List<AttendanceLocationDto> locations =
        model.getLocations() != null
            ? model.getLocations().stream().map(this::mapLocationToDto).collect(Collectors.toList())
            : new ArrayList<>();

    return AttendanceLocationSettingsResponse.builder()
        .id(model.getId())
        .isGlobalEnabled(model.getIsGlobalEnabled())
        .locations(locations)
        .build();
  }

  private AttendanceLocationDto mapLocationToDto(AttendanceLocationModel loc) {
    return AttendanceLocationDto.builder()
        .id(loc.getId())
        .name(loc.getName())
        .boundary(
            loc.getBoundary() != null
                ? loc.getBoundary().stream()
                    .map(
                        p ->
                            LatLngDto.builder()
                                .latitude(p.getLatitude())
                                .longitude(p.getLongitude())
                                .build())
                    .collect(Collectors.toList())
                : new ArrayList<>())
        .build();
  }
}
