package com.norton.backend.services.attendance;

import com.norton.backend.dto.request.CreateAttendanceRequest;
import com.norton.backend.dto.request.UpdateAttendanceStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AllOfficersReportResponse;
import com.norton.backend.dto.responses.attendances.AttendanceExportResponse;
import com.norton.backend.dto.responses.attendances.AttendanceImportErrorResponse;
import com.norton.backend.dto.responses.attendances.AttendanceImportResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSessionResponse;
import com.norton.backend.dto.responses.attendances.AttendanceStatusDataResponse;
import com.norton.backend.dto.responses.attendances.AttendanceStatusResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSummaryDataResponse;
import com.norton.backend.dto.responses.attendances.AttendanceSummaryResponse;
import com.norton.backend.dto.responses.attendances.CreateAttendanceResponse;
import com.norton.backend.dto.responses.attendances.OfficerAttendanceDailyDetailResponse;
import com.norton.backend.dto.responses.attendances.OfficerAttendanceMonthlyHistoryResponse;
import com.norton.backend.dto.responses.attendances.OfficerAttendanceTodayScanInfoResponse;
import com.norton.backend.dto.responses.attendances.UpdateAttendanceResponse;
import com.norton.backend.dto.responses.organization.DepartmentResponseDto;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.exceptions.UnauthorizedException;
import com.norton.backend.models.AttendanceModel;
import com.norton.backend.models.AttendanceSessionModel;
import com.norton.backend.models.AttendanceStatusModel;
import com.norton.backend.models.DepartmentModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.AttendanceSessionRepository;
import com.norton.backend.repositories.AttendanceStatusRepository;
import com.norton.backend.repositories.DepartmentRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.services.security.OfficeAccessService;
import com.norton.backend.services.shift.ShiftResolutionService;
import com.norton.backend.services.shift.ShiftResolutionService.ShiftWindow;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

  private static final List<String> MORNING_SHIFT_NAMES =
      List.of("ážœáŸáž“áž–áŸ’ážšáž¹áž€", "Morning Shift");
  private static final List<String> AFTERNOON_SHIFT_NAMES =
      List.of("ážœáŸáž“ážšážŸáŸ€áž›", "Afternoon Shift");
  private static final Set<String> ALLOWED_STATUS_CODES =
      Set.of("PRESENT", "ABSENT", "LATE", "HALF_DAY");
  private static final DateTimeFormatter EXPORT_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private final AttendanceRepository attendanceRepository;
  private final AttendanceSessionRepository attendanceSessionRepository;
  private final OfficerRepository officerRepository;
  private final DepartmentRepository departmentRepository;
  private final AttendanceStatusRepository attendanceStatusRepository;
  private final ShiftResolutionService shiftResolutionService;
  private final OfficeAccessService officeAccessService;

  @Value("${attendance.scan.timezone:Asia/Phnom_Penh}")
  private String scanTimezone;

  @Override
  public PageResponse<AttendanceResponse> getAllAttendance(
      int page,
      int size,
      String search,
      LocalDate date,
      String department,
      String status,
      String viewMode) {
    Pageable pageable = PageRequest.of(page, size);
    DateRange dateRange = resolveDateRange(date, viewMode);

    Page<AttendanceResponse> result =
        attendanceRepository.findAllAttendanceFiltered(
            pageable,
            defaultStartDate(dateRange.start()),
            defaultEndDate(dateRange.end()),
            officeAccessService.currentOfficeScopeIdOrNull(),
            toSearchPattern(search),
            toLowerTrimmed(department),
            toLowerTrimmed(status));
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
    LocalDateTime currentDateTime = LocalDateTime.now(zoneId);
    LocalDate currentDate = currentDateTime.toLocalDate();

    OfficerModel targetOfficer =
        officerRepository
            .findByIdWithPosition(targetOfficerId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", targetOfficerId));
    officeAccessService.assertCanAccessOfficer(targetOfficer);
    ShiftDecision currentShift = resolveCurrentShift(targetOfficer, currentDateTime);
    LocalDate attendanceDate =
        currentShift != null && currentShift.shiftDate() != null
            ? currentShift.shiftDate()
            : currentDate;
    AttendanceModel attendance =
        attendanceRepository.findByOfficerIdAndDate(targetOfficerId, attendanceDate).orElse(null);
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
      String checkInLocal = checkInDateTime.atZone(zoneId).toOffsetDateTime().toString();

      if (checkedOut) {
        LocalDateTime checkOutDateTime =
            LocalDateTime.of(sessionDate, relevantSession.getCheckOut());
        checkOutTime = checkOutDateTime.atZone(zoneId).toInstant();
        String checkOutLocal = checkOutDateTime.atZone(zoneId).toOffsetDateTime().toString();
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
            .checkInLocal(
                checkedIn
                    ? (attendance != null
                        ? LocalDateTime.of(attendance.getDate(), relevantSession.getCheckIn())
                            .atZone(zoneId)
                            .toOffsetDateTime()
                            .toString()
                        : null)
                    : null)
            .checkOutTime(checkOutTime)
            .checkOutLocal(
                checkedOut
                    ? (attendance != null
                        ? LocalDateTime.of(attendance.getDate(), relevantSession.getCheckOut())
                            .atZone(zoneId)
                            .toOffsetDateTime()
                            .toString()
                        : null)
                    : null)
            .workingHours(formatWorkingHours(workedDuration))
            .shift(resolveShiftLabel(currentShift, relevantSession))
            .displayText(formatDisplayText(workedDuration))
            .attendanceDate(attendanceDate)
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
    OfficerModel targetOfficer =
        officerRepository
            .findByIdWithPosition(targetOfficerId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", targetOfficerId));
    officeAccessService.assertCanAccessOfficer(targetOfficer);

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
  @Transactional(readOnly = true)
  public AllOfficersReportResponse getAllOfficersReport(String onOffice, LocalDate onTodayDate) {
    if (onOffice == null || onOffice.isBlank()) {
      throw new BadRequestException("Office name is required");
    }
    if (onTodayDate == null) {
      throw new BadRequestException("Date is required");
    }

    String normalizedOffice = onOffice.trim();
    DepartmentModel requestedOffice = resolveOfficeByName(normalizedOffice);

    Long scopeOfficeId = officeAccessService.currentOfficeScopeIdOrNull();
    if (scopeOfficeId != null && !scopeOfficeId.equals(requestedOffice.getId())) {
      throw new UnauthorizedException("You can only access officers in your own office");
    }

    List<OfficerModel> officers = officerRepository.findByOffice_Id(requestedOffice.getId());

    // If current user is a regular officer on mobile, only return their own information
    String currentRole = officeAccessService.currentUser().getRole().getRoleName();
    if ("ROLE_OFFICER".equals(currentRole)) {
      Long currentUserId = officeAccessService.currentUser().getId();
      OfficerModel selfOfficer =
          officerRepository.findByUserIdWithPosition(currentUserId).orElse(null);

      if (selfOfficer == null) {
        return buildAllOfficersReportResponse(Collections.emptyList(), Collections.emptyList());
      }

      AttendanceModel attendance =
          attendanceRepository
              .findByOfficerIdAndDate(selfOfficer.getId(), onTodayDate)
              .orElse(null);
      boolean isPresent = attendance != null && attendance.getCheckIn() != null;

      AllOfficersReportResponse.AttendanceStaffReportItem item =
          AllOfficersReportResponse.AttendanceStaffReportItem.builder()
              .officerId(selfOfficer.getUuid())
              .name(selfOfficer.getFirstNameEn() + " " + selfOfficer.getLastNameEn())
              .role(selfOfficer.getPosition() != null ? selfOfficer.getPosition().getName() : null)
              .departmentId(
                  selfOfficer.getOffice() != null ? selfOfficer.getOffice().getId() : null)
              .departmentName(
                  selfOfficer.getOffice() != null ? selfOfficer.getOffice().getName() : null)
              .isPresent(isPresent)
              .imageUrl(selfOfficer.getImageUrl())
              .checkInTime(formatTime(attendance != null ? attendance.getCheckIn() : null))
              .checkOutTime(formatTime(attendance != null ? attendance.getCheckOut() : null))
              .build();

      int presentCount = isPresent ? 1 : 0;
      int totalStaff = 1;
      int absentCount = totalStaff - presentCount;
      int attendancePercentage = presentCount == 0 ? 0 : 100;

      return AllOfficersReportResponse.builder()
          .summary(
              AllOfficersReportResponse.Summary.builder()
                  .totalStaff(totalStaff)
                  .presentCount(presentCount)
                  .absentCount(absentCount)
                  .attendancePercentage(attendancePercentage)
                  .build())
          .attendanceStaffs(Collections.singletonList(item))
          .build();
    }

    Map<Long, AttendanceModel> attendancesByOfficerId =
        attendanceRepository
            .findAllByDateAndOfficer_Office_Id(onTodayDate, requestedOffice.getId())
            .stream()
            .filter(att -> att.getOfficer() != null)
            .collect(
                Collectors.toMap(
                    att -> att.getOfficer().getId(),
                    att -> att,
                    (existing, replacement) -> existing));

    List<AllOfficersReportResponse.AttendanceStaffReportItem> attendanceStaffs =
        officers.stream()
            .map(
                officer -> {
                  AttendanceModel attendance = attendancesByOfficerId.get(officer.getId());
                  boolean isPresent = attendance != null && attendance.getCheckIn() != null;

                  return AllOfficersReportResponse.AttendanceStaffReportItem.builder()
                      .officerId(officer.getUuid())
                      .name(officer.getFirstNameEn() + " " + officer.getLastNameEn())
                      .role(officer.getPosition() != null ? officer.getPosition().getName() : null)
                      .departmentId(
                          officer.getOffice() != null ? officer.getOffice().getId() : null)
                      .departmentName(
                          officer.getOffice() != null ? officer.getOffice().getName() : null)
                      .isPresent(isPresent)
                      .imageUrl(officer.getImageUrl())
                      .checkInTime(formatTime(attendance != null ? attendance.getCheckIn() : null))
                      .checkOutTime(
                          formatTime(attendance != null ? attendance.getCheckOut() : null))
                      .build();
                })
            .collect(Collectors.toList());

    return buildAllOfficersReportResponse(
        attendanceStaffs, List.of(toDepartmentDto(requestedOffice)));
  }

  @Override
  @Transactional(readOnly = true)
  public AllOfficersReportResponse getAllOfficersAttendanceReport(
      LocalDate onDate, Long adminOfficerId) {
    if (onDate == null) {
      throw new BadRequestException("Date is required");
    }
    if (adminOfficerId == null) {
      throw new BadRequestException("adminOfficerId is required");
    }

    OfficerModel adminOfficer =
        officerRepository
            .findByIdWithPosition(adminOfficerId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", adminOfficerId));

    String currentRole = officeAccessService.currentUser().getRole().getRoleName();
    if ("ROLE_OFFICER".equals(currentRole)) {
      throw new UnauthorizedException("Officers cannot access admin-scoped attendance reports");
    }

    officeAccessService.assertCanAccessOfficer(adminOfficer);

    DepartmentModel adminOffice = resolveOfficerDepartment(adminOfficer);
    if (adminOffice == null) {
      throw new BadRequestException("Admin officer is not assigned to an office");
    }

    List<OfficerModel> officers = officerRepository.findByOffice_Id(adminOffice.getId());

    Map<Long, AttendanceModel> attendancesByOfficerId =
        attendanceRepository.findAllByDateAndOfficer_Office_Id(onDate, adminOffice.getId()).stream()
            .filter(att -> att.getOfficer() != null)
            .collect(
                Collectors.toMap(
                    att -> att.getOfficer().getId(),
                    att -> att,
                    (existing, replacement) -> existing));

    List<AllOfficersReportResponse.AttendanceStaffReportItem> attendanceStaffs =
        officers.stream()
            .map(
                officer -> {
                  AttendanceModel attendance = attendancesByOfficerId.get(officer.getId());
                  boolean isPresent = attendance != null && attendance.getCheckIn() != null;

                  return AllOfficersReportResponse.AttendanceStaffReportItem.builder()
                      .officerId(officer.getUuid())
                      .name(officer.getFirstNameEn() + " " + officer.getLastNameEn())
                      .role(officer.getPosition() != null ? officer.getPosition().getName() : null)
                      .departmentId(resolveOfficerOfficeId(officer))
                      .departmentName(resolveOfficerOfficeName(officer))
                      .isPresent(isPresent)
                      .imageUrl(officer.getImageUrl())
                      .checkInTime(formatTime(attendance != null ? attendance.getCheckIn() : null))
                      .checkOutTime(
                          formatTime(attendance != null ? attendance.getCheckOut() : null))
                      .build();
                })
            .collect(Collectors.toList());

    return buildAllOfficersReportResponse(attendanceStaffs, List.of(toDepartmentDto(adminOffice)));
  }

  private AllOfficersReportResponse buildAllOfficersReportResponse(
      List<AllOfficersReportResponse.AttendanceStaffReportItem> attendanceStaffs,
      List<DepartmentResponseDto> officerAllowViewDepartmetn) {
    int totalStaff = attendanceStaffs.size();
    int presentCount =
        (int)
            attendanceStaffs.stream()
                .filter(AllOfficersReportResponse.AttendanceStaffReportItem::isPresent)
                .count();
    int absentCount = Math.max(totalStaff - presentCount, 0);
    int attendancePercentage =
        totalStaff == 0 ? 0 : (int) Math.round((presentCount * 100.0) / totalStaff);

    return AllOfficersReportResponse.builder()
        .summary(
            AllOfficersReportResponse.Summary.builder()
                .totalStaff(totalStaff)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .attendancePercentage(attendancePercentage)
                .build())
        .officerAllowViewDepartmetn(officerAllowViewDepartmetn)
        .attendanceStaffs(attendanceStaffs)
        .build();
  }

  private DepartmentResponseDto toDepartmentDto(DepartmentModel department) {
    if (department == null) {
      return null;
    }
    return DepartmentResponseDto.builder()
        .id(department.getId())
        .uuid(department.getUuid())
        .name(department.getName())
        .officerCount(officerRepository.countByOffice_Id(department.getId()))
        .status(department.getStatus() != null ? department.getStatus().name().toLowerCase() : null)
        .description(department.getDescription())
        .build();
  }

  private DepartmentModel resolveOfficerDepartment(OfficerModel officer) {
    if (officer == null) {
      return null;
    }
    if (officer.getOffice() != null) {
      return officer.getOffice();
    }
    if (officer.getPosition() != null) {
      return officer.getPosition().getDepartment();
    }
    return null;
  }

  private static String resolveOfficerOfficeName(OfficerModel officer) {
    if (officer == null) {
      return null;
    }
    if (officer.getOffice() != null && officer.getOffice().getName() != null) {
      return officer.getOffice().getName();
    }
    if (officer.getPosition() != null && officer.getPosition().getDepartment() != null) {
      return officer.getPosition().getDepartment().getName();
    }
    return null;
  }

  private DepartmentModel resolveOfficeByName(String officeName) {
    return departmentRepository
        .findByNameIgnoreCase(officeName)
        .orElseThrow(() -> new ResourceNotFoundException("Office", "name", officeName));
  }

  private static Long resolveOfficerOfficeId(OfficerModel officer) {
    if (officer == null) {
      return null;
    }
    if (officer.getOffice() != null) {
      return officer.getOffice().getId();
    }
    if (officer.getPosition() != null && officer.getPosition().getDepartment() != null) {
      return officer.getPosition().getDepartment().getId();
    }
    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public com.norton.backend.dto.responses.attendances.OfficerReportResponse getOfficerReport(
      Long officerId, LocalDate onDate) {
    if (officerId == null) {
      throw new BadRequestException("officerId is required");
    }
    if (onDate == null) {
      throw new BadRequestException("date is required");
    }

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(officerId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", officerId));
    return buildOfficerReportResponse(officer, onDate);
  }

  @Override
  @Transactional(readOnly = true)
  public com.norton.backend.dto.responses.attendances.OfficerReportResponse getOfficerReport(
      String officerUuid, LocalDate onDate) {
    if (officerUuid == null || officerUuid.isBlank()) {
      throw new BadRequestException("officerUuid is required");
    }
    if (onDate == null) {
      throw new BadRequestException("date is required");
    }

    OfficerModel officer =
        officerRepository
            .findByUuidWithPosition(officerUuid)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "uuid", officerUuid));
    return buildOfficerReportResponse(officer, onDate);
  }

  private com.norton.backend.dto.responses.attendances.OfficerReportResponse
      buildOfficerReportResponse(OfficerModel officer, LocalDate onDate) {
    officeAccessService.assertCanAccessOfficer(officer);

    String currentRole = officeAccessService.currentUser().getRole().getRoleName();
    if ("ROLE_OFFICER".equals(currentRole)) {
      Long currentUserId = officeAccessService.currentUser().getId();
      if (officer.getUser() == null || !currentUserId.equals(officer.getUser().getId())) {
        throw new UnauthorizedException("Officers can only access their own report");
      }
    }

    AttendanceModel attendance =
        attendanceRepository.findByOfficerIdAndDate(officer.getId(), onDate).orElse(null);

    com.norton.backend.dto.responses.attendances.OfficerReportResponse.Attendance attendanceDto;
    if (attendance != null) {
      attendanceDto =
          com.norton.backend.dto.responses.attendances.OfficerReportResponse.Attendance.builder()
              .date(attendance.getDate())
              .status(attendance.getStatus() != null ? attendance.getStatus().getCode() : null)
              .checkInTime(formatTime(attendance.getCheckIn()))
              .checkOutTime(formatTime(attendance.getCheckOut()))
              .workingHours(attendance.getTotalWorkMin())
              .lateMinutes(attendance.getTotalLateMin())
              .build();
    } else {
      attendanceDto =
          com.norton.backend.dto.responses.attendances.OfficerReportResponse.Attendance.builder()
              .date(onDate)
              .status("ABSENT")
              .checkInTime(null)
              .checkOutTime(null)
              .workingHours(0)
              .lateMinutes(0)
              .build();
    }

    return com.norton.backend.dto.responses.attendances.OfficerReportResponse.builder()
        .officerId(officer.getUuid())
        .name(officer.getFirstNameEn() + " " + officer.getLastNameEn())
        .role(officer.getPosition() != null ? officer.getPosition().getName() : null)
        .departmentId(officer.getOffice() != null ? officer.getOffice().getId() : null)
        .departmentName(officer.getOffice() != null ? officer.getOffice().getName() : null)
        .imageUrl(officer.getImageUrl())
        .phone(officer.getPhone())
        .email(officer.getEmail())
        .attendance(attendanceDto)
        .build();
  }

  private String formatTime(LocalDateTime dateTime) {
    return dateTime == null ? null : dateTime.format(TIME_FORMAT);
  }

  @Override
  @Transactional
  public CreateAttendanceResponse createAttendance(CreateAttendanceRequest request) {
    validateTimeRange(request.getCheckIn(), request.getCheckOut());

    if (attendanceRepository.existsByOfficerIdAndDate(request.getOfficerId(), request.getDate())) {
      throw new ConflictException("Attendance already exists for this officer on the given date");
    }

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(request.getOfficerId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Officer", "id", request.getOfficerId()));
    officeAccessService.assertCanAccessOfficer(officer);

    AttendanceStatusModel status = resolveStatus(request.getStatus());
    AttendanceModel attendance =
        upsertAttendanceModel(
            AttendanceModel.builder().build(),
            officer,
            request.getDate(),
            request.getCheckIn(),
            request.getCheckOut(),
            status,
            request.getNotes());

    AttendanceModel savedAttendance = attendanceRepository.save(attendance);
    return toCreateResponse(savedAttendance);
  }

  @Override
  @Transactional
  public UpdateAttendanceResponse updateAttendanceStatus(
      Long id, UpdateAttendanceStatusRequest request) {
    validateTimeRange(request.getCheckIn(), request.getCheckOut());

    AttendanceModel attendance =
        attendanceRepository
            .findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(request.getOfficerId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Officer", "id", request.getOfficerId()));
    officeAccessService.assertCanAccessOfficer(attendance.getOfficer());
    officeAccessService.assertCanAccessOfficer(officer);

    attendanceRepository
        .findByOfficerIdAndDate(request.getOfficerId(), request.getDate())
        .filter(existing -> !existing.getId().equals(id))
        .ifPresent(
            existing -> {
              throw new ConflictException(
                  "Attendance already exists for this officer on the given date");
            });

    AttendanceStatusModel status = resolveStatus(request.getStatus());
    upsertAttendanceModel(
        attendance,
        officer,
        request.getDate(),
        request.getCheckIn(),
        request.getCheckOut(),
        status,
        request.getNotes());

    AttendanceModel updatedAttendance = attendanceRepository.save(attendance);
    return toUpdateResponse(updatedAttendance);
  }

  @Override
  @Transactional(readOnly = true)
  public AttendanceExportResponse exportAttendance(
      LocalDate date, String department, String status, String search, String viewMode) {
    DateRange dateRange = resolveDateRange(date, viewMode);
    List<AttendanceResponse> data =
        attendanceRepository.findAllAttendanceFilteredForExport(
            defaultStartDate(dateRange.start()),
            defaultEndDate(dateRange.end()),
            officeAccessService.currentOfficeScopeIdOrNull(),
            toSearchPattern(search),
            toLowerTrimmed(department),
            toLowerTrimmed(status));

    byte[] fileContent = buildAttendanceWorkbook(data);
    LocalDate filenameDate = date != null ? date : LocalDate.now(resolveScanZoneId());

    return AttendanceExportResponse.builder()
        .filename("attendance-" + EXPORT_DATE_FORMAT.format(filenameDate) + ".xlsx")
        .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .content(fileContent)
        .build();
  }

  @Override
  public AttendanceImportResponse importAttendance(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("file is required");
    }

    List<AttendanceImportErrorResponse> errors = new ArrayList<>();
    int created = 0;
    int updated = 0;
    int failed = 0;

    try (InputStream in = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(in)) {
      Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
      if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
        return AttendanceImportResponse.builder()
            .created(0)
            .updated(0)
            .failed(0)
            .errors(List.of())
            .build();
      }

      Map<String, Integer> headers = resolveHeaderIndexes(sheet.getRow(0));

      for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row == null || isRowEmpty(row)) {
          continue;
        }

        try {
          String officerCode = readRequiredString(row, headers, "officerCode", rowIndex);
          LocalDate attendanceDate = readRequiredDate(row, headers, "date", rowIndex);
          LocalTime checkIn = readRequiredTime(row, headers, "checkIn", rowIndex);
          LocalTime checkOut = readRequiredTime(row, headers, "checkOut", rowIndex);
          String statusValue = readRequiredString(row, headers, "status", rowIndex);
          String notes = readOptionalString(row, headers, "notes");

          validateTimeRange(checkIn, checkOut);

          OfficerModel officer =
              officerRepository
                  .findByOfficerCode(officerCode)
                  .orElseThrow(
                      () -> new BadRequestException("Unknown officerCode: " + officerCode));
          officer = officerRepository.findByIdWithPosition(officer.getId()).orElse(officer);
          officeAccessService.assertCanAccessOfficer(officer);

          AttendanceStatusModel status = resolveStatus(statusValue);
          AttendanceModel attendance =
              attendanceRepository
                  .findByOfficerIdAndDate(officer.getId(), attendanceDate)
                  .orElse(AttendanceModel.builder().build());

          boolean exists = attendance.getId() != null;
          upsertAttendanceModel(
              attendance, officer, attendanceDate, checkIn, checkOut, status, notes);
          attendanceRepository.save(attendance);

          if (exists) {
            updated++;
          } else {
            created++;
          }
        } catch (Exception ex) {
          failed++;
          errors.add(
              AttendanceImportErrorResponse.builder()
                  .row(rowIndex + 1)
                  .message(ex.getMessage())
                  .build());
        }
      }
    } catch (IOException ex) {
      throw new BadRequestException("Failed to read import file");
    }

    return AttendanceImportResponse.builder()
        .created(created)
        .updated(updated)
        .failed(failed)
        .errors(errors)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public OfficerAttendanceMonthlyHistoryResponse getOfficerAttendanceMonthlyHistory(
      Long officerId, String onMonth) {
    if (officerId == null) {
      throw new BadRequestException("officerId is required");
    }
    if (onMonth == null || onMonth.isBlank()) {
      throw new BadRequestException("onMonth is required (format: yyyy-MM)");
    }

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(officerId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", officerId));
    officeAccessService.assertCanAccessOfficer(officer);

    try {
      String[] parts = onMonth.trim().split("-");
      if (parts.length != 2) {
        throw new BadRequestException("Invalid month format. Use yyyy-MM");
      }
      int year = Integer.parseInt(parts[0]);
      int month = Integer.parseInt(parts[1]);
      LocalDate startDate = LocalDate.of(year, month, 1);
      LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

      List<AttendanceModel> attendances =
          attendanceRepository.findAllByOfficerIdAndDateBetween(officerId, startDate, endDate);

      List<LocalDate> presentDates = new ArrayList<>();
      List<LocalDate> absentDates = new ArrayList<>();
      List<LocalDate> lateDates = new ArrayList<>();

      for (AttendanceModel attendance : attendances) {
        if (attendance.getCheckIn() == null) {
          absentDates.add(attendance.getDate());
        } else if (attendance.getTotalLateMin() != null && attendance.getTotalLateMin() > 0) {
          lateDates.add(attendance.getDate());
        } else {
          presentDates.add(attendance.getDate());
        }
      }

      OfficerAttendanceMonthlyHistoryResponse.MonthlySummary summary =
          OfficerAttendanceMonthlyHistoryResponse.MonthlySummary.builder()
              .present(presentDates.size())
              .absent(absentDates.size())
              .late(lateDates.size())
              .build();

      return OfficerAttendanceMonthlyHistoryResponse.builder()
          .month(onMonth)
          .summary(summary)
          .presentDates(presentDates)
          .absentDates(absentDates)
          .lateDates(lateDates)
          .build();
    } catch (NumberFormatException | java.time.DateTimeException ex) {
      throw new BadRequestException("Invalid month format. Use yyyy-MM");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public OfficerAttendanceDailyDetailResponse getOfficerAttendanceDailyDetail(
      Long officerId, LocalDate onDate) {
    if (officerId == null) {
      throw new BadRequestException("officerId is required");
    }
    if (onDate == null) {
      throw new BadRequestException("onDate is required");
    }

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(officerId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", officerId));
    officeAccessService.assertCanAccessOfficer(officer);

    AttendanceModel attendance =
        attendanceRepository
            .findByOfficerIdAndDate(officerId, onDate)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance", "date", onDate));

    String status = attendance.getStatus() != null ? attendance.getStatus().getCode() : "UNKNOWN";
    LocalTime checkInTime =
        attendance.getCheckIn() != null ? attendance.getCheckIn().toLocalTime() : null;
    LocalTime checkOutTime =
        attendance.getCheckOut() != null ? attendance.getCheckOut().toLocalTime() : null;

    String workingHours = "00:00";
    if (checkInTime != null && checkOutTime != null) {
      long minutes = attendance.getTotalWorkMin() != null ? attendance.getTotalWorkMin() : 0;
      long hours = minutes / 60;
      long mins = minutes % 60;
      workingHours = String.format("%02d:%02d", hours, mins);
    }

    Integer lateMinutes = attendance.getTotalLateMin() != null ? attendance.getTotalLateMin() : 0;

    List<OfficerAttendanceDailyDetailResponse.TimelineEntry> timeline = new ArrayList<>();
    if (checkInTime != null) {
      timeline.add(
          OfficerAttendanceDailyDetailResponse.TimelineEntry.builder()
              .time(checkInTime)
              .type("check_in")
              .build());
    }
    if (checkOutTime != null) {
      timeline.add(
          OfficerAttendanceDailyDetailResponse.TimelineEntry.builder()
              .time(checkOutTime)
              .type("check_out")
              .build());
    }

    OfficerAttendanceDailyDetailResponse.OfficeInfo officeInfo = null;
    if (officer.getOffice() != null) {
      officeInfo =
          OfficerAttendanceDailyDetailResponse.OfficeInfo.builder()
              .id(officer.getOffice().getId())
              .name(officer.getOffice().getName())
              .build();
    }

    return OfficerAttendanceDailyDetailResponse.builder()
        .date(attendance.getDate())
        .status(status)
        .checkIn(checkInTime)
        .checkOut(checkOutTime)
        .workingHours(workingHours)
        .lateMinutes(lateMinutes)
        .office(officeInfo)
        .timeline(timeline)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public OfficerAttendanceTodayScanInfoResponse getOfficerAttendanceTodayScanInfo(Long officerId) {
    if (officerId == null) {
      throw new BadRequestException("officerId is required");
    }

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(officerId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", officerId));
    officeAccessService.assertCanAccessOfficer(officer);

    ZoneId zoneId = resolveScanZoneId();
    LocalDate today = LocalDate.now(zoneId);

    AttendanceModel attendance =
        attendanceRepository
            .findByOfficerIdAndDate(officerId, today)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance", "date", today));

    LocalTime checkInTime =
        attendance.getCheckIn() != null ? attendance.getCheckIn().toLocalTime() : null;
    LocalTime checkOutTime =
        attendance.getCheckOut() != null ? attendance.getCheckOut().toLocalTime() : null;

    String workingDuration = "00:00:00";
    if (checkInTime != null && checkOutTime != null) {
      long totalSeconds =
          attendance.getTotalWorkMin() != null ? attendance.getTotalWorkMin() * 60 : 0;
      long hours = totalSeconds / 3600;
      long minutes = (totalSeconds % 3600) / 60;
      long seconds = totalSeconds % 60;
      workingDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    String status = attendance.getStatus() != null ? attendance.getStatus().getCode() : "PRESENT";

    List<OfficerAttendanceTodayScanInfoResponse.TimelineEntry> timeline = new ArrayList<>();
    if (checkInTime != null) {
      timeline.add(
          OfficerAttendanceTodayScanInfoResponse.TimelineEntry.builder()
              .time(checkInTime.toString())
              .title("Check In")
              .type("check_in")
              .build());
    }
    if (checkOutTime != null) {
      timeline.add(
          OfficerAttendanceTodayScanInfoResponse.TimelineEntry.builder()
              .time(checkOutTime.toString())
              .title("Check Out")
              .type("check_out")
              .build());
    }

    return OfficerAttendanceTodayScanInfoResponse.builder()
        .date(today)
        .checkIn(checkInTime)
        .checkOut(checkOutTime)
        .workingDuration(workingDuration)
        .status(status)
        .timeline(timeline)
        .build();
  }

  @Override
  @Transactional
  public void deleteTodayAttendance(Long officerId) {
    Long targetOfficerId = resolveTargetOfficerId(officerId);
    ZoneId zoneId = resolveScanZoneId();
    LocalDate today = LocalDate.now(zoneId);

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(targetOfficerId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", targetOfficerId));
    officeAccessService.assertCanAccessOfficer(officer);

    Optional<ShiftDecision> currentShift =
        Optional.ofNullable(resolveCurrentShift(officer, LocalDateTime.now(zoneId)));
    LocalDate attendanceDate = currentShift.map(ShiftDecision::shiftDate).orElse(today);

    // delete both the current attendance date and today's date if they differ,
    // because overnight shifts may span midnight and status may still resolve to the previous shift
    // date.
    if (!attendanceDate.equals(today)) {
      deleteAttendanceForOfficerAndDate(targetOfficerId, attendanceDate);
    }
    deleteAttendanceForOfficerAndDate(targetOfficerId, today);
  }

  private void deleteAttendanceForOfficerAndDate(Long officerId, LocalDate date) {
    if (date == null) {
      return;
    }
    AttendanceModel attendance =
        attendanceRepository.findByOfficerIdAndDate(officerId, date).orElse(null);
    if (attendance == null) {
      return;
    }
    attendanceSessionRepository.deleteByAttendanceId(attendance.getId());
    attendanceRepository.delete(attendance);
  }

  @Override
  @Transactional
  public void deleteAllAttendancesForDate(LocalDate date) {
    if (date == null) {
      throw new BadRequestException("Date is required for attendance reset");
    }

    List<AttendanceModel> attendances = attendanceRepository.findAllByDate(date);
    if (attendances.isEmpty()) {
      return;
    }
    List<Long> attendanceIds = attendances.stream().map(AttendanceModel::getId).toList();
    attendanceSessionRepository.deleteByAttendanceIdIn(attendanceIds);
    attendanceRepository.deleteByDate(date);
  }

  @Override
  @Transactional
  public void deleteAllAttendancesForToday() {
    deleteAllAttendancesForDate(LocalDate.now(resolveScanZoneId()));
  }

  private AttendanceModel upsertAttendanceModel(
      AttendanceModel attendance,
      OfficerModel officer,
      LocalDate date,
      LocalTime checkInTime,
      LocalTime checkOutTime,
      AttendanceStatusModel status,
      String notes) {
    LocalDateTime checkIn = LocalDateTime.of(date, checkInTime);
    LocalDateTime checkOut = LocalDateTime.of(date, checkOutTime);

    attendance.setOfficer(officer);
    attendance.setDate(date);
    attendance.setCheckIn(checkIn);
    attendance.setCheckOut(checkOut);
    attendance.setTotalWorkMin((int) Duration.between(checkIn, checkOut).toMinutes());
    attendance.setTotalLateMin(calculateLateMinutes(checkInTime));
    attendance.setStatus(status);
    attendance.setNotes(notes);
    return attendance;
  }

  private CreateAttendanceResponse toCreateResponse(AttendanceModel attendance) {
    OfficerModel officer = attendance.getOfficer();
    return CreateAttendanceResponse.builder()
        .id(attendance.getId())
        .officerId(officer.getId())
        .imageUrl(officer.getImageUrl())
        .date(attendance.getDate())
        .checkIn(attendance.getCheckIn())
        .checkOut(attendance.getCheckOut())
        .totalWorkMin(attendance.getTotalWorkMin())
        .totalLateMin(attendance.getTotalLateMin())
        .status(attendance.getStatus() != null ? attendance.getStatus().getName() : null)
        .firstName(officer.getFirstName())
        .lastName(officer.getLastName())
        .department(officer.getPosition().getDepartment().getName())
        .officerCode(officer.getOfficerCode())
        .sessions(fetchSessions(attendance.getId()))
        .build();
  }

  private UpdateAttendanceResponse toUpdateResponse(AttendanceModel attendance) {
    OfficerModel officer = attendance.getOfficer();
    return UpdateAttendanceResponse.builder()
        .id(attendance.getId())
        .officerId(officer.getId())
        .imageUrl(officer.getImageUrl())
        .date(attendance.getDate())
        .checkIn(attendance.getCheckIn())
        .checkOut(attendance.getCheckOut())
        .totalWorkMin(attendance.getTotalWorkMin())
        .totalLateMin(attendance.getTotalLateMin())
        .status(attendance.getStatus() != null ? attendance.getStatus().getName() : null)
        .firstName(officer.getFirstName())
        .lastName(officer.getLastName())
        .department(officer.getPosition().getDepartment().getName())
        .officerCode(officer.getOfficerCode())
        .sessions(fetchSessions(attendance.getId()))
        .build();
  }

  private List<AttendanceSessionResponse> fetchSessions(Long attendanceId) {
    return attendanceSessionRepository.findByAttendanceId(attendanceId).stream()
        .map(this::toSessionResponse)
        .toList();
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

  private AttendanceSessionResponse toSessionResponse(AttendanceSessionModel session) {
    AttendanceSessionResponse response = new AttendanceSessionResponse();
    response.setId(session.getId());
    response.setShiftName(session.getShift() != null ? session.getShift().getName() : null);
    response.setCheckIn(session.getCheckIn() != null ? session.getCheckIn().toString() : null);
    response.setCheckOut(session.getCheckOut() != null ? session.getCheckOut().toString() : null);
    response.setStatus(session.getStatus());
    return response;
  }

  private byte[] buildAttendanceWorkbook(List<AttendanceResponse> data) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("attendance");
      String[] headers =
          new String[] {
            "officerCode",
            "firstName",
            "lastName",
            "department",
            "date",
            "checkIn",
            "checkOut",
            "totalWorkMin",
            "totalLateMin",
            "status"
          };

      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.length; i++) {
        headerRow.createCell(i).setCellValue(headers[i]);
      }

      int rowIdx = 1;
      for (AttendanceResponse item : data) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(defaultString(item.getOfficerCode()));
        row.createCell(1).setCellValue(defaultString(item.getFirstName()));
        row.createCell(2).setCellValue(defaultString(item.getLastName()));
        row.createCell(3).setCellValue(defaultString(item.getDepartment()));
        row.createCell(4).setCellValue(item.getDate() != null ? item.getDate().toString() : "");
        row.createCell(5)
            .setCellValue(
                item.getCheckIn() != null ? item.getCheckIn().toLocalTime().toString() : "");
        row.createCell(6)
            .setCellValue(
                item.getCheckOut() != null ? item.getCheckOut().toLocalTime().toString() : "");
        row.createCell(7).setCellValue(item.getTotalWorkMin() != null ? item.getTotalWorkMin() : 0);
        row.createCell(8).setCellValue(item.getTotalLateMin() != null ? item.getTotalLateMin() : 0);
        row.createCell(9).setCellValue(defaultString(item.getStatus()));
      }

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return out.toByteArray();
    } catch (IOException ex) {
      throw new BadRequestException("Failed to generate export file");
    }
  }

  private DateRange resolveDateRange(LocalDate date, String viewMode) {
    String mode = trimToNull(viewMode);
    if (mode == null || "daily".equalsIgnoreCase(mode)) {
      if (date == null) {
        return new DateRange(null, null);
      }
      return new DateRange(date, date);
    }

    if ("monthly".equalsIgnoreCase(mode)) {
      LocalDate targetDate = date != null ? date : LocalDate.now(resolveScanZoneId());
      return new DateRange(
          targetDate.withDayOfMonth(1), targetDate.withDayOfMonth(targetDate.lengthOfMonth()));
    }

    return new DateRange(null, null);
  }

  private AttendanceStatusModel resolveStatus(String statusValue) {
    String normalizedCode = normalizeStatusCode(statusValue);
    if (!ALLOWED_STATUS_CODES.contains(normalizedCode)) {
      throw new BadRequestException("Invalid status. Allowed: Present, Absent, Late, Half-day");
    }

    return attendanceStatusRepository
        .findByCode(normalizedCode)
        .or(() -> attendanceStatusRepository.findByNameIgnoreCase(statusValue.trim()))
        .orElseThrow(() -> new BadRequestException("Attendance status not found: " + statusValue));
  }

  private String normalizeStatusCode(String statusValue) {
    if (statusValue == null || statusValue.isBlank()) {
      throw new BadRequestException("status is required");
    }
    return statusValue.trim().toUpperCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
  }

  private void validateTimeRange(LocalTime checkIn, LocalTime checkOut) {
    if (checkIn == null || checkOut == null) {
      throw new BadRequestException("checkIn and checkOut are required");
    }
    if (!checkOut.isAfter(checkIn)) {
      throw new BadRequestException("checkOut must be after checkIn");
    }
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

  private ShiftDecision resolveCurrentShift(OfficerModel officer, LocalDateTime currentDateTime) {
    Optional<ShiftWindow> shiftWindow =
        shiftResolutionService.resolveOfficerShift(officer, currentDateTime);
    return shiftWindow
        .map(window -> new ShiftDecision(window.shift(), window.shiftDate()))
        .orElse(null);
  }

  private String resolveShiftLabel(
      ShiftDecision currentShift, AttendanceSessionModel relevantSession) {
    if (currentShift != null) {
      return currentShift.shift().getName();
    }

    if (relevantSession != null && relevantSession.getShift() != null) {
      return relevantSession.getShift().getName();
    }

    return null;
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

  private int calculateLateMinutes(LocalTime checkIn) {
    LocalTime officialStart = LocalTime.of(8, 0);
    if (!checkIn.isAfter(officialStart)) {
      return 0;
    }
    return (int) Duration.between(officialStart, checkIn).toMinutes();
  }

  private ZoneId resolveScanZoneId() {
    try {
      return ZoneId.of(scanTimezone);
    } catch (Exception ex) {
      throw new BadRequestException(
          "Invalid attendance scan timezone configuration: " + scanTimezone);
    }
  }

  private Map<String, Integer> resolveHeaderIndexes(Row headerRow) {
    if (headerRow == null) {
      throw new BadRequestException("Header row is missing");
    }

    Map<String, Integer> indexByHeader = new LinkedHashMap<>();
    for (Cell cell : headerRow) {
      String name = normalizeHeader(readCellAsString(cell));
      if (name != null) {
        indexByHeader.put(name, cell.getColumnIndex());
      }
    }

    List<String> requiredHeaders = List.of("officercode", "date", "checkin", "checkout", "status");
    for (String required : requiredHeaders) {
      if (!indexByHeader.containsKey(required)) {
        throw new BadRequestException("Missing required column: " + required);
      }
    }
    return indexByHeader;
  }

  private boolean isRowEmpty(Row row) {
    for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
      if (i < 0) {
        continue;
      }
      Cell cell = row.getCell(i);
      if (cell != null && !readCellAsString(cell).isBlank()) {
        return false;
      }
    }
    return true;
  }

  private String readRequiredString(
      Row row, Map<String, Integer> headers, String key, int rowIndex) {
    String value = readOptionalString(row, headers, key);
    if (value == null || value.isBlank()) {
      throw new BadRequestException("Row " + (rowIndex + 1) + ": " + key + " is required");
    }
    return value;
  }

  private String readOptionalString(Row row, Map<String, Integer> headers, String key) {
    Integer idx = headers.get(normalizeHeader(key));
    if (idx == null) {
      return null;
    }
    Cell cell = row.getCell(idx);
    String value = cell == null ? null : readCellAsString(cell);
    return trimToNull(value);
  }

  private LocalDate readRequiredDate(
      Row row, Map<String, Integer> headers, String key, int rowIndex) {
    Integer idx = headers.get(normalizeHeader(key));
    if (idx == null) {
      throw new BadRequestException("Row " + (rowIndex + 1) + ": " + key + " is required");
    }
    Cell cell = row.getCell(idx);
    if (cell == null) {
      throw new BadRequestException("Row " + (rowIndex + 1) + ": " + key + " is required");
    }
    return parseDateCell(cell, rowIndex, key);
  }

  private LocalTime readRequiredTime(
      Row row, Map<String, Integer> headers, String key, int rowIndex) {
    Integer idx = headers.get(normalizeHeader(key));
    if (idx == null) {
      throw new BadRequestException("Row " + (rowIndex + 1) + ": " + key + " is required");
    }
    Cell cell = row.getCell(idx);
    if (cell == null) {
      throw new BadRequestException("Row " + (rowIndex + 1) + ": " + key + " is required");
    }
    return parseTimeCell(cell, rowIndex, key);
  }

  private LocalDate parseDateCell(Cell cell, int rowIndex, String key) {
    try {
      if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
        return cell.getLocalDateTimeCellValue().toLocalDate();
      }

      String value = trimToNull(readCellAsString(cell));
      if (value == null) {
        throw new BadRequestException("Row " + (rowIndex + 1) + ": " + key + " is required");
      }
      return LocalDate.parse(value);
    } catch (DateTimeParseException ex) {
      throw new BadRequestException("Row " + (rowIndex + 1) + ": invalid date format");
    }
  }

  private LocalTime parseTimeCell(Cell cell, int rowIndex, String key) {
    try {
      if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
        return cell.getLocalDateTimeCellValue().toLocalTime().withSecond(0).withNano(0);
      }

      String value = trimToNull(readCellAsString(cell));
      if (value == null) {
        throw new BadRequestException("Row " + (rowIndex + 1) + ": " + key + " is required");
      }

      try {
        return LocalTime.parse(value, DateTimeFormatter.ofPattern("H:mm"));
      } catch (DateTimeParseException ignored) {
        return LocalTime.parse(value, DateTimeFormatter.ofPattern("H:mm:ss"));
      }
    } catch (DateTimeParseException ex) {
      throw new BadRequestException("Row " + (rowIndex + 1) + ": invalid time format");
    }
  }

  private String readCellAsString(Cell cell) {
    DataFormatter formatter = new DataFormatter();
    return formatter.formatCellValue(cell).trim();
  }

  private String normalizeHeader(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    return normalized.isBlank() ? null : normalized;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String toLowerTrimmed(String value) {
    String normalized = trimToNull(value);
    return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
  }

  private String toSearchPattern(String value) {
    String normalized = toLowerTrimmed(value);
    return normalized.isEmpty() ? "%" : "%" + normalized + "%";
  }

  private LocalDate defaultStartDate(LocalDate value) {
    return value == null ? LocalDate.of(1900, 1, 1) : value;
  }

  private LocalDate defaultEndDate(LocalDate value) {
    return value == null ? LocalDate.of(2999, 12, 31) : value;
  }

  private String defaultString(String value) {
    return value == null ? "" : value;
  }

  private record ShiftDecision(ShiftModel shift, LocalDate shiftDate) {}

  private record DateRange(LocalDate start, LocalDate end) {}
}
