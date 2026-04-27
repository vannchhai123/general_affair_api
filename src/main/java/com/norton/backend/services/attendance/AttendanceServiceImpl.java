package com.norton.backend.services.attendance;

import com.norton.backend.dto.request.CreateAttendanceRequest;
import com.norton.backend.dto.request.UpdateAttendanceStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
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

  private final AttendanceRepository attendanceRepository;
  private final AttendanceSessionRepository attendanceSessionRepository;
  private final OfficerRepository officerRepository;
  private final AttendanceStatusRepository attendanceStatusRepository;
  private final ShiftRepository shiftRepository;

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
    validateTimeRange(request.getCheckIn(), request.getCheckOut());

    if (attendanceRepository.existsByOfficerIdAndDate(request.getOfficerId(), request.getDate())) {
      throw new ConflictException("Attendance already exists for this officer on the given date");
    }

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(request.getOfficerId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Officer", "id", request.getOfficerId()));

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

  private ShiftDecision resolveCurrentShift(LocalTime currentTime) {
    ShiftModel morningShift = resolveShiftByNames(MORNING_SHIFT_NAMES);
    ShiftModel afternoonShift = resolveShiftByNames(AFTERNOON_SHIFT_NAMES);

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

    return "MORNING";
  }

  private ShiftModel resolveShiftByNames(List<String> shiftNames) {
    for (String shiftName : shiftNames) {
      ShiftModel shift = shiftRepository.findByName(shiftName).orElse(null);
      if (shift != null) {
        return shift;
      }
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

  private record ShiftDecision(ShiftModel shift) {}

  private record DateRange(LocalDate start, LocalDate end) {}
}
