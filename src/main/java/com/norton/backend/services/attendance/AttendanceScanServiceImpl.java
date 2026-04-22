package com.norton.backend.services.attendance;

import com.norton.backend.dto.request.AttendanceScanRequest;
import com.norton.backend.dto.responses.attendances.AttendanceScanDataResponse;
import com.norton.backend.dto.responses.attendances.AttendanceScanSuccessResponse;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.exceptions.AttendanceScanException;
import com.norton.backend.models.AttendanceModel;
import com.norton.backend.models.AttendanceSessionModel;
import com.norton.backend.models.AttendanceStatusModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.QrSessionCheckInModel;
import com.norton.backend.models.QrSessionModel;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.AttendanceSessionRepository;
import com.norton.backend.repositories.AttendanceStatusRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.QrSessionCheckInRepository;
import com.norton.backend.repositories.QrSessionRepository;
import com.norton.backend.repositories.ShiftRepository;
import com.norton.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceScanServiceImpl implements AttendanceScanService {
  private static final String ACTION_CHECK_IN = "CHECK_IN";
  private static final String ACTION_CHECK_OUT = "CHECK_OUT";
  private static final String ACTION_ALREADY_COMPLETED = "ALREADY_COMPLETED";
  private static final String ACTION_INVALID_TIME = "INVALID_TIME";

  private final QrSessionRepository qrSessionRepository;
  private final QrSessionCheckInRepository qrSessionCheckInRepository;
  private final OfficerRepository officerRepository;
  private final AttendanceRepository attendanceRepository;
  private final AttendanceSessionRepository attendanceSessionRepository;
  private final AttendanceStatusRepository attendanceStatusRepository;
  private final ShiftRepository shiftRepository;
  private final JwtService jwtService;

  @Override
  @Transactional
  public AttendanceScanSuccessResponse submitScan(AttendanceScanRequest request) {
    Claims claims = validateQrToken(request.getToken());
    String sessionId = claims.get("sessionId", String.class);
    QrSessionModel session =
        qrSessionRepository
            .findByTokenWithCreatedBy(sessionId)
            .orElseThrow(
                () ->
                    new AttendanceScanException(
                        HttpStatus.NOT_FOUND, "Invalid QR token", "QR_INVALID"));

    validateSession(session);

    OfficerModel officer =
        officerRepository
            .findByOfficerCode(request.getOfficerCode())
            .filter(foundOfficer -> foundOfficer.getPosition() != null)
            .filter(foundOfficer -> foundOfficer.getStatus() == OfficerStatus.ACTIVE)
            .orElseThrow(
                () ->
                    new AttendanceScanException(
                        HttpStatus.FORBIDDEN,
                        "Officer is not allowed to check in",
                        "OFFICER_NOT_ALLOWED"));

    LocalDateTime currentDateTime = LocalDateTime.now(ZoneOffset.UTC);
    Instant currentTimestamp = currentDateTime.toInstant(ZoneOffset.UTC);
    ShiftDecision shiftDecision = resolveShift(currentDateTime.toLocalTime());
    if (shiftDecision == null) {
      saveScanAudit(
          session,
          officer,
          request.getDeviceId(),
          currentDateTime,
          ACTION_INVALID_TIME,
          "invalid-time");
      return buildResponse(
          false,
          "Invalid scan time",
          null,
          session,
          officer,
          currentTimestamp,
          ACTION_INVALID_TIME,
          null);
    }

    AttendanceModel attendance = getOrCreateAttendance(officer, currentDateTime.toLocalDate());
    AttendanceSessionModel existingSession =
        attendanceSessionRepository
            .findByAttendanceIdAndShiftId(attendance.getId(), shiftDecision.shift().getId())
            .orElse(null);

    String action;
    if (existingSession == null) {
      action = ACTION_CHECK_IN;
      createShiftSession(attendance, shiftDecision.shift(), officer, currentDateTime.toLocalTime());
    } else if (existingSession.getCheckOut() == null) {
      if (!currentDateTime.toLocalTime().isAfter(existingSession.getCheckIn())) {
        saveScanAudit(
            session,
            officer,
            request.getDeviceId(),
            currentDateTime,
            ACTION_INVALID_TIME,
            "invalid-time");
        return buildResponse(
            false,
            "Invalid scan time",
            attendance,
            session,
            officer,
            currentTimestamp,
            ACTION_INVALID_TIME,
            shiftDecision.label());
      }
      action = ACTION_CHECK_OUT;
      existingSession.setCheckOut(currentDateTime.toLocalTime());
      existingSession.setStatus("COMPLETED");
      attendanceSessionRepository.save(existingSession);
    } else {
      action = ACTION_ALREADY_COMPLETED;
    }

    AttendanceModel updatedAttendance = refreshAttendanceSummary(attendance);
    saveScanAudit(
        session,
        officer,
        request.getDeviceId(),
        currentDateTime,
        action,
        deriveScanStatus(action, updatedAttendance));

    String message =
        switch (action) {
          case ACTION_CHECK_IN -> "Check-in recorded successfully";
          case ACTION_CHECK_OUT -> "Check-out recorded successfully";
          case ACTION_ALREADY_COMPLETED -> "Attendance already completed for this shift";
          default -> "Attendance scan processed";
        };

    return buildResponse(
        true,
        message,
        updatedAttendance,
        session,
        officer,
        currentTimestamp,
        action,
        shiftDecision.label());
  }

  private Claims validateQrToken(String token) {
    try {
      Claims claims = jwtService.extractAllClaims(token);
      String subject = claims.getSubject();
      String purpose = claims.get("purpose", String.class);
      String sessionId = claims.get("sessionId", String.class);

      if (!"qr-session-kiosk".equals(subject)
          || !"qr-kiosk".equals(purpose)
          || sessionId == null
          || sessionId.isBlank()) {
        throw new AttendanceScanException(HttpStatus.BAD_REQUEST, "Invalid QR token", "QR_INVALID");
      }

      return claims;
    } catch (ExpiredJwtException ex) {
      throw new AttendanceScanException(HttpStatus.GONE, "QR token expired", "QR_EXPIRED");
    } catch (JwtException | IllegalArgumentException ex) {
      throw new AttendanceScanException(HttpStatus.BAD_REQUEST, "Invalid QR token", "QR_INVALID");
    }
  }

  private void validateSession(QrSessionModel session) {
    if (session.getStatus() == null || !"active".equalsIgnoreCase(session.getStatus())) {
      throw new AttendanceScanException(
          HttpStatus.GONE, "Attendance session is inactive", "SESSION_INACTIVE");
    }

    if (session.getValidUntil() != null
        && !session.getValidUntil().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
      throw new AttendanceScanException(
          HttpStatus.GONE, "Attendance session is inactive", "SESSION_INACTIVE");
    }
  }

  private ShiftDecision resolveShift(LocalTime time) {
    ShiftModel morningShift =
        shiftRepository
            .findByName("Morning Shift")
            .orElseThrow(
                () ->
                    new AttendanceScanException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Shift configuration missing: Morning Shift",
                        "SHIFT_CONFIG_MISSING"));
    ShiftModel afternoonShift =
        shiftRepository
            .findByName("Afternoon Shift")
            .orElseThrow(
                () ->
                    new AttendanceScanException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Shift configuration missing: Afternoon Shift",
                        "SHIFT_CONFIG_MISSING"));

    if (isWithinShift(time, morningShift)) {
      return new ShiftDecision(morningShift, "MORNING");
    }
    if (isWithinShift(time, afternoonShift)) {
      return new ShiftDecision(afternoonShift, "AFTERNOON");
    }
    return null;
  }

  private boolean isWithinShift(LocalTime time, ShiftModel shift) {
    return shift.getStartTime() != null
        && shift.getEndTime() != null
        && !time.isBefore(shift.getStartTime())
        && !time.isAfter(shift.getEndTime());
  }

  private AttendanceModel getOrCreateAttendance(OfficerModel officer, LocalDate date) {
    return attendanceRepository
        .findByOfficerOfficerCodeAndDate(officer.getOfficerCode(), date)
        .orElseGet(
            () ->
                attendanceRepository.save(
                    AttendanceModel.builder()
                        .officer(officer)
                        .date(date)
                        .totalWorkMin(0)
                        .totalLateMin(0)
                        .status(resolveAttendanceStatus(false))
                        .notes("Auto-created from QR scan")
                        .build()));
  }

  private void createShiftSession(
      AttendanceModel attendance, ShiftModel shift, OfficerModel officer, LocalTime checkInTime) {
    String status =
        calculateLateMinutes(checkInTime, shift.getStartTime()) > 0 ? "LATE" : "PRESENT";

    attendanceSessionRepository.save(
        AttendanceSessionModel.builder()
            .uuid(UUID.randomUUID().toString())
            .attendance(attendance)
            .shift(shift)
            .checkIn(checkInTime)
            .checkOut(null)
            .status(status)
            .createdBy(officer)
            .build());
  }

  private AttendanceModel refreshAttendanceSummary(AttendanceModel attendance) {
    List<AttendanceSessionModel> sessions =
        attendanceSessionRepository.findByAttendanceId(attendance.getId());

    LocalDate date = attendance.getDate();
    LocalTime earliestCheckIn =
        sessions.stream()
            .map(AttendanceSessionModel::getCheckIn)
            .filter(java.util.Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElse(null);
    LocalTime latestCheckOut =
        sessions.stream()
            .map(AttendanceSessionModel::getCheckOut)
            .filter(java.util.Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(null);

    int totalWorkMinutes =
        sessions.stream()
            .filter(s -> s.getCheckIn() != null && s.getCheckOut() != null)
            .filter(s -> s.getCheckOut().isAfter(s.getCheckIn()))
            .mapToInt(s -> (int) Duration.between(s.getCheckIn(), s.getCheckOut()).toMinutes())
            .sum();

    int totalLateMinutes =
        sessions.stream()
            .filter(s -> s.getCheckIn() != null && s.getShift() != null)
            .mapToInt(s -> calculateLateMinutes(s.getCheckIn(), s.getShift().getStartTime()))
            .sum();

    attendance.setCheckIn(earliestCheckIn != null ? LocalDateTime.of(date, earliestCheckIn) : null);
    attendance.setCheckOut(latestCheckOut != null ? LocalDateTime.of(date, latestCheckOut) : null);
    attendance.setTotalWorkMin(totalWorkMinutes);
    attendance.setTotalLateMin(totalLateMinutes);
    attendance.setStatus(resolveAttendanceStatus(totalLateMinutes > 0));
    return attendanceRepository.save(attendance);
  }

  private String deriveScanStatus(String action, AttendanceModel attendance) {
    return switch (action) {
      case ACTION_CHECK_IN -> attendance.getTotalLateMin() > 0 ? "late" : "checked-in";
      case ACTION_CHECK_OUT -> "checked-out";
      case ACTION_ALREADY_COMPLETED -> "already-completed";
      default -> "invalid-time";
    };
  }

  private void saveScanAudit(
      QrSessionModel session,
      OfficerModel officer,
      String deviceId,
      LocalDateTime scannedAt,
      String action,
      String status) {
    qrSessionCheckInRepository.save(
        QrSessionCheckInModel.builder()
            .qrSession(session)
            .officer(officer)
            .action(action.toLowerCase())
            .status(status)
            .scannedAt(scannedAt)
            .deviceInfo(deviceId)
            .build());
  }

  private AttendanceScanSuccessResponse buildResponse(
      boolean success,
      String message,
      AttendanceModel attendance,
      QrSessionModel session,
      OfficerModel officer,
      Instant scannedAt,
      String action,
      String shift) {
    return AttendanceScanSuccessResponse.builder()
        .success(success)
        .message(message)
        .data(
            AttendanceScanDataResponse.builder()
                .attendanceId(attendance != null ? attendance.getId() : null)
                .sessionId(session.getToken())
                .officerCode(officer.getOfficerCode())
                .officerName(officer.getFirstName() + " " + officer.getLastName())
                .action(action)
                .status(
                    attendance != null && attendance.getStatus() != null
                        ? attendance.getStatus().getCode()
                        : null)
                .timestamp(scannedAt)
                .shift(shift)
                .scannedAt(scannedAt)
                .location(session.getLocation())
                .build())
        .build();
  }

  private AttendanceStatusModel resolveAttendanceStatus(boolean isLate) {
    String statusCode = isLate ? "LATE" : "PRESENT";

    return attendanceStatusRepository
        .findByCode(statusCode)
        .orElseThrow(
            () ->
                new AttendanceScanException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Attendance status configuration missing: " + statusCode,
                    "ATTENDANCE_STATUS_MISSING"));
  }

  private int calculateLateMinutes(LocalTime checkInTime, LocalTime shiftStartTime) {
    if (shiftStartTime == null || !checkInTime.isAfter(shiftStartTime)) {
      return 0;
    }
    return (int) Duration.between(shiftStartTime, checkInTime).toMinutes();
  }

  private record ShiftDecision(ShiftModel shift, String label) {}
}
