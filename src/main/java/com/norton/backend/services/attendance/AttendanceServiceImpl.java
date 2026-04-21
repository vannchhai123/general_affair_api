package com.norton.backend.services.attendance;

import com.norton.backend.dto.request.CreateAttendanceRequest;
import com.norton.backend.dto.request.UpdateAttendanceStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSessionResponse;
import com.norton.backend.dto.responses.attendances.CreateAttendanceResponse;
import com.norton.backend.dto.responses.attendances.UpdateAttendanceResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.AttendanceModel;
import com.norton.backend.models.AttendanceStatusModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.AttendanceSessionRepository;
import com.norton.backend.repositories.AttendanceStatusRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
  private final AttendanceRepository attendanceRepository;
  private final AttendanceSessionRepository attendanceSessionRepository;
  private final OfficerRepository officerRepository;
  private final AttendanceStatusRepository attendanceStatusRepository;

  @Override
  public PageResponse<AttendanceResponse> getAllAttendance(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

    Page<AttendanceResponse> result = attendanceRepository.findAllAttendance(pageable);
    attachSessions(result.getContent());

    return PageResponse.<AttendanceResponse>builder()
        .content(result.getContent())
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .last(result.isLast())
        .build();
  }

  @Override
  @Transactional
  public CreateAttendanceResponse createAttendance(CreateAttendanceRequest request) {
    if (!request.getCheckOut().isAfter(request.getCheckIn())) {
      throw new BadRequestException("check_out must be after check_in");
    }

    if (attendanceRepository.existsByOfficerIdAndDate(request.getOfficerId(), request.getDate())) {
      throw new ConflictException("Attendance already exists for this officer on the given date");
    }

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(request.getOfficerId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Officer", "id", request.getOfficerId()));

    AttendanceStatusModel status =
        attendanceStatusRepository
            .findByNameIgnoreCase(request.getStatus())
            .orElseThrow(
                () ->
                    new BadRequestException("Attendance status not found: " + request.getStatus()));

    LocalDateTime checkIn = LocalDateTime.of(request.getDate(), request.getCheckIn());
    LocalDateTime checkOut = LocalDateTime.of(request.getDate(), request.getCheckOut());

    AttendanceModel attendance =
        AttendanceModel.builder()
            .officer(officer)
            .date(request.getDate())
            .checkIn(checkIn)
            .checkOut(checkOut)
            .totalWorkMin((int) Duration.between(checkIn, checkOut).toMinutes())
            .totalLateMin(calculateLateMinutes(request.getCheckIn()))
            .status(status)
            .notes(request.getNotes())
            .build();

    AttendanceModel savedAttendance = attendanceRepository.save(attendance);

    return CreateAttendanceResponse.builder()
        .id(savedAttendance.getId())
        .officerId(officer.getId())
        .firstName(officer.getFirstName())
        .lastName(officer.getLastName())
        .department(officer.getPosition().getDepartment().getName())
        .employeeCode(officer.getOfficerCode())
        .date(savedAttendance.getDate())
        .checkIn(request.getCheckIn().toString())
        .checkOut(request.getCheckOut().toString())
        .totalWorkMinutes(savedAttendance.getTotalWorkMin())
        .totalLateMinutes(savedAttendance.getTotalLateMin())
        .status(status.getName())
        .sessions(new ArrayList<>())
        .build();
  }

  @Override
  @Transactional
  public UpdateAttendanceResponse updateAttendanceStatus(
      Long id, UpdateAttendanceStatusRequest request) {
    AttendanceModel attendance =
        attendanceRepository
            .findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

    AttendanceStatusModel status = resolveStatus(request.getStatus());
    OfficerModel approver = resolveCurrentApprover();

    attendance.setStatus(status);
    attendance.setApprovedBy(approver);

    AttendanceModel updatedAttendance = attendanceRepository.save(attendance);

    return UpdateAttendanceResponse.builder()
        .id(updatedAttendance.getId())
        .officerId(updatedAttendance.getOfficer().getId())
        .date(updatedAttendance.getDate())
        .totalWorkMinutes(updatedAttendance.getTotalWorkMin())
        .totalLateMinutes(updatedAttendance.getTotalLateMin())
        .status(status.getCode())
        .firstName(updatedAttendance.getOfficer().getFirstName())
        .lastName(updatedAttendance.getOfficer().getLastName())
        .department(updatedAttendance.getOfficer().getPosition().getDepartment().getName())
        .approvedBy(approver != null ? approver.getId() : null)
        .approvedAt(updatedAttendance.getUpdatedAt())
        .build();
  }

  private void attachSessions(List<AttendanceResponse> attendances) {
    if (attendances.isEmpty()) {
      return;
    }

    List<Long> attendanceIds = attendances.stream().map(AttendanceResponse::getId).toList();

    Map<Long, List<AttendanceSessionResponse>> sessionsByAttendanceId =
        attendanceSessionRepository.findAllByAttendanceIds(attendanceIds).stream()
            .collect(
                Collectors.groupingBy(
                    session -> session.getAttendance().getId(),
                    Collectors.mapping(this::toSessionResponse, Collectors.toList())));

    attendances.forEach(
        attendance ->
            attendance.setSessions(
                sessionsByAttendanceId.getOrDefault(attendance.getId(), Collections.emptyList())));
  }

  private AttendanceSessionResponse toSessionResponse(
      com.norton.backend.models.AttendanceSessionModel session) {
    AttendanceSessionResponse response = new AttendanceSessionResponse();
    response.setId(session.getId());
    response.setShiftName(session.getShift() != null ? session.getShift().getName() : null);
    response.setCheckIn(session.getCheckIn() != null ? session.getCheckIn().toString() : null);
    response.setCheckOut(session.getCheckOut() != null ? session.getCheckOut().toString() : null);
    response.setStatus(session.getStatus());
    return response;
  }

  private int calculateLateMinutes(LocalTime checkIn) {
    LocalTime officialStart = LocalTime.of(8, 0);
    if (!checkIn.isAfter(officialStart)) {
      return 0;
    }
    return (int) Duration.between(officialStart, checkIn).toMinutes();
  }

  private AttendanceStatusModel resolveStatus(String statusValue) {
    return attendanceStatusRepository
        .findByCode(statusValue.toUpperCase())
        .or(() -> attendanceStatusRepository.findByNameIgnoreCase(statusValue))
        .orElseThrow(() -> new BadRequestException("Attendance status not found: " + statusValue));
  }

  private OfficerModel resolveCurrentApprover() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof UserModel currentUser)) {
      return null;
    }

    return officerRepository.findByUserIdWithPosition(currentUser.getId()).orElse(null);
  }
}
