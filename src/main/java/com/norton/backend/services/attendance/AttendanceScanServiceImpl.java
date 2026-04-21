package com.norton.backend.services.attendance;

import com.norton.backend.dto.request.AttendanceScanRequest;
import com.norton.backend.dto.responses.attendances.AttendanceScanDataResponse;
import com.norton.backend.dto.responses.attendances.AttendanceScanSuccessResponse;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.exceptions.AttendanceScanException;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.QrSessionCheckInModel;
import com.norton.backend.models.QrSessionModel;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.QrSessionCheckInRepository;
import com.norton.backend.repositories.QrSessionRepository;
import com.norton.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceScanServiceImpl implements AttendanceScanService {
  private static final String DEFAULT_SCAN_ACTION = "check-in";

  private final QrSessionRepository qrSessionRepository;
  private final QrSessionCheckInRepository qrSessionCheckInRepository;
  private final OfficerRepository officerRepository;
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

    String normalizedAction = DEFAULT_SCAN_ACTION;
    String storedStatus = mapStoredStatus(normalizedAction, request.getScannedAt());

    QrSessionCheckInModel savedCheckIn =
        qrSessionCheckInRepository.save(
            QrSessionCheckInModel.builder()
                .qrSession(session)
                .officer(officer)
                .action(normalizedAction)
                .status(storedStatus)
                .scannedAt(LocalDateTime.ofInstant(request.getScannedAt(), ZoneOffset.UTC))
                .deviceInfo(request.getDeviceId())
                .build());

    return AttendanceScanSuccessResponse.builder()
        .success(true)
        .message("Attendance recorded successfully")
        .data(
            AttendanceScanDataResponse.builder()
                .attendanceId(savedCheckIn.getId())
                .sessionId(session.getToken())
                .officerCode(officer.getOfficerCode())
                .officerName(officer.getFirstName() + " " + officer.getLastName())
                .action(normalizedAction)
                .status(mapResponseStatus(storedStatus))
                .scannedAt(request.getScannedAt())
                .location(session.getLocation())
                .build())
        .build();
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

  private String mapStoredStatus(String action, Instant scannedAt) {
    if ("check-out".equals(action)) {
      return "checked-out";
    }

    Instant eightAm =
        scannedAt.atZone(ZoneOffset.UTC).toLocalDate().atTime(8, 0).toInstant(ZoneOffset.UTC);
    return scannedAt.isAfter(eightAm) ? "late" : "checked-in";
  }

  private String mapResponseStatus(String storedStatus) {
    return switch (storedStatus) {
      case "checked-in" -> "on-time";
      case "late" -> "late";
      case "checked-out" -> "checked-out";
      default -> storedStatus;
    };
  }
}
