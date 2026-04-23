package com.norton.backend.services.attendance;

import com.norton.backend.dto.request.CreateAttendanceRequest;
import com.norton.backend.dto.request.UpdateAttendanceStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSessionResponse;
import com.norton.backend.dto.responses.attendances.AttendanceStatusDataResponse;
import com.norton.backend.dto.responses.attendances.AttendanceStatusResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSummaryDataResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSummaryResponse;
import com.norton.backend.dto.responses.attendances.CreateAttendanceResponse;
import com.norton.backend.dto.responses.attendances.UpdateAttendanceResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.exceptions.UnauthorizedException;
import com.norton.backend.models.AttendanceModel;
import com.norton.backend.models.AttendanceSessionModel;
import com.norton.backend.models.AttendanceStatusModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.AttendanceSessionRepository;
import com.norton.backend.repositories.AttendanceStatusRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.ShiftRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
  private final ShiftRepository shiftRepository;

  @Value("${attendance.scan.timezone:Asia/Phnom_Penh}")
  private String scanTimezone;

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
  @Transactional(readOnly = true)
  public AttendanceStatusResponse getMyAttendanceStatus(Long officerId) {
    Long targetOfficerId = resolveTargetOfficerId(officerId);
    ZoneId zoneId = resolveScanZoneId();
    LocalDate currentDate = LocalDate.now(zoneId);
    LocalTime currentTime = LocalTime.now(zoneId);

    AttendanceModel attendance =
        attendanceRepository.findByOfficerIdAndDate(targetOfficerId, currentDate).orElse(null);

    ShiftDecision currentShift = resolveCurrentShift(currentTime);
    AttendanceSessionModel relevantSession = resolveRelevantSession(attendance, currentShift);

    boolean checkedIn = relevantSession != null && relevantSession.getCheckIn() != null;
    boolean checkedOut = relevantSession != null && relevantSession.getCheckOut() != null;

    Instant checkInTime = null;
    Instant checkOutTime = null;
    Duration workedDuration = Duration.ZERO;

    if (checkedIn) {
      LocalDate sessionDate = attendance != null ? attendance.getDate() : currentDate;
      LocalDateTime checkInDateTime = LocalDateTime.of(sessionDate, relevantSession.getCheckIn());
      checkInTime = checkInDateTime.atZone(zoneId).toInstant();

      if (checkedOut) {
        LocalDateTime checkOutDateTime =
            LocalDateTime.of(sessionDate, relevantSession.getCheckOut());
        checkOutTime = checkOutDateTime.atZone(zoneId).toInstant();
        workedDuration = safeDurationBetween(checkInDateTime, checkOutDateTime);
      } else {
        workedDuration = safeDurationBetween(checkInDateTime, LocalDateTime.now(zoneId));
      }
    }

    AttendanceStatusDataResponse data =
        AttendanceStatusDataResponse.builder()
            .officerId(targetOfficerId)
            .isCheckedIn(checkedIn)
            .isCheckedOut(checkedOut)
            .checkInTime(checkInTime)
            .checkOutTime(checkOutTime)
            .workingHours(formatWorkingHours(workedDuration))
            .shift(resolveShiftLabel(currentShift, relevantSession))
            .displayText(formatDisplayText(workedDuration))
            .build();

    return AttendanceStatusResponse.builder()
        .success(true)
        .message("Status fetched successfully")
        .data(data)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public AttendanceSummaryResponse getMyAttendanceSummary(Long officerId) {
    Long targetOfficerId = resolveTargetOfficerId(officerId);
    ZoneId zoneId = resolveScanZoneId();
    LocalDate today = LocalDate.now(zoneId);
    LocalDate startOfMonth = today.withDayOfMonth(1);
    LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

    List<AttendanceModel> monthlyAttendances =
        attendanceRepository.findAllByOfficerIdAndDateBetween(
            targetOfficerId, startOfMonth, endOfMonth);

    int lateCount =
        (int)
            monthlyAttendances.stream()
                .filter(attendance -> attendance.getCheckIn() != null)
                .filter(attendance -> attendance.getTotalLateMin() != null)
                .filter(attendance -> attendance.getTotalLateMin() > 0)
                .count();

    int presentCount =
        (int)
            monthlyAttendances.stream()
                .filter(attendance -> attendance.getCheckIn() != null)
                .filter(
                    attendance ->
                        attendance.getTotalLateMin() == null || attendance.getTotalLateMin() <= 0)
                .count();

    int totalWorkingDays = calculateWorkingDays(startOfMonth, endOfMonth);
    int absentCount = Math.max(totalWorkingDays - presentCount - lateCount, 0);

    AttendanceSummaryDataResponse data =
        AttendanceSummaryDataResponse.builder()
            .officerId(targetOfficerId)
            .presentCount(presentCount)
            .absentCount(absentCount)
            .lateCount(lateCount)
            .totalWorkingDays(totalWorkingDays)
            .build();

    return AttendanceSummaryResponse.builder()
        .success(true)
        .message("Attendance summary fetched successfully")
        .data(data)
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

  private UserModel resolveCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof UserModel currentUser)) {
      throw new UnauthorizedException("Unauthorized access");
    }
    return currentUser;
  }

  private Long resolveTargetOfficerId(Long officerId) {
    if (officerId != null) {
      return officerRepository
          .findById(officerId)
          .map(OfficerModel::getId)
          .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", officerId));
    }

    UserModel currentUser = resolveCurrentUser();
    return officerRepository
        .findByUserIdWithPosition(currentUser.getId())
        .map(OfficerModel::getId)
        .orElseThrow(() -> new ResourceNotFoundException("Officer", "userId", currentUser.getId()));
  }

  private AttendanceSessionModel resolveRelevantSession(
      AttendanceModel attendance, ShiftDecision currentShift) {
    if (attendance == null) {
      return null;
    }

    if (currentShift != null) {
      return attendanceSessionRepository
          .findByAttendanceIdAndShiftId(attendance.getId(), currentShift.shift().getId())
          .orElse(null);
    }

    List<AttendanceSessionModel> sessions =
        attendanceSessionRepository.findByAttendanceId(attendance.getId());
    if (sessions.isEmpty()) {
      return null;
    }

    return sessions.stream()
        .filter(session -> session.getCheckIn() != null && session.getCheckOut() == null)
        .max(Comparator.comparing(AttendanceSessionModel::getCheckIn))
        .orElseGet(
            () ->
                sessions.stream()
                    .filter(session -> session.getCheckOut() != null)
                    .max(Comparator.comparing(AttendanceSessionModel::getCheckOut))
                    .orElse(null));
  }

  private ShiftDecision resolveCurrentShift(LocalTime currentTime) {
    ShiftModel morningShift = shiftRepository.findByName("Morning Shift").orElse(null);
    ShiftModel afternoonShift = shiftRepository.findByName("Afternoon Shift").orElse(null);

    if (morningShift != null && isWithinShift(currentTime, morningShift)) {
      return new ShiftDecision(morningShift);
    }

    if (afternoonShift != null && isWithinShift(currentTime, afternoonShift)) {
      return new ShiftDecision(afternoonShift);
    }

    return null;
  }

  private boolean isWithinShift(LocalTime time, ShiftModel shift) {
    return shift.getStartTime() != null
        && shift.getEndTime() != null
        && !time.isBefore(shift.getStartTime())
        && !time.isAfter(shift.getEndTime());
  }

  private String resolveShiftLabel(
      ShiftDecision currentShift, AttendanceSessionModel relevantSession) {
    if (currentShift != null) {
      return currentShift.shift().getName();
    }

    if (relevantSession != null && relevantSession.getShift() != null) {
      return relevantSession.getShift().getName();
    }

    return "Morning Shift";
  }

  private Duration safeDurationBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null || end.isBefore(start)) {
      return Duration.ZERO;
    }
    return Duration.between(start, end);
  }

  private String formatWorkingHours(Duration duration) {
    long totalMinutes = Math.max(0, duration.toMinutes());
    long hours = totalMinutes / 60;
    long minutes = totalMinutes % 60;
    return hours + "h " + minutes + "m";
  }

  private String formatDisplayText(Duration duration) {
    long totalSeconds = Math.max(0, duration.getSeconds());
    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }

  private int calculateWorkingDays(LocalDate fromDate, LocalDate toDate) {
    int workingDays = 0;
    long days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
    for (long i = 0; i < days; i++) {
      LocalDate date = fromDate.plusDays(i);
      switch (date.getDayOfWeek()) {
        case SATURDAY, SUNDAY -> {}
        default -> workingDays++;
      }
    }
    return workingDays;
  }

  private java.time.ZoneId resolveScanZoneId() {
    try {
      return java.time.ZoneId.of(scanTimezone);
    } catch (Exception ex) {
      throw new BadRequestException(
          "Invalid attendance scan timezone configuration: " + scanTimezone);
    }
  }

  private record ShiftDecision(ShiftModel shift) {}
}
