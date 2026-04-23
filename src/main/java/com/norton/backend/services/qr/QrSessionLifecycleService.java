package com.norton.backend.services.qr;

import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.QrSessionModel;
import com.norton.backend.repositories.QrSessionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrSessionLifecycleService {
  private static final LocalTime MORNING_START = LocalTime.of(6, 0);
  private static final LocalTime MORNING_END = LocalTime.of(12, 30);
  private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
  private static final LocalTime AFTERNOON_END = LocalTime.of(17, 30);

  private final QrSessionRepository qrSessionRepository;

  @Value("${attendance.scan.timezone:Asia/Phnom_Penh}")
  private String sessionTimezone;

  public ZoneId resolveZoneId() {
    return ZoneId.of(sessionTimezone);
  }

  public LocalDateTime now() {
    return LocalDateTime.now(resolveZoneId());
  }

  public ShiftWindow getCurrentShiftWindow(LocalDateTime now) {
    LocalTime time = now.toLocalTime();

    if (!time.isBefore(MORNING_START) && !time.isAfter(MORNING_END)) {
      return new ShiftWindow(
          "morning", "Morning Shift", now.toLocalDate(), MORNING_START, MORNING_END);
    }

    if (!time.isBefore(AFTERNOON_START) && !time.isAfter(AFTERNOON_END)) {
      return new ShiftWindow(
          "afternoon", "Afternoon Shift", now.toLocalDate(), AFTERNOON_START, AFTERNOON_END);
    }

    return null;
  }

  @Transactional
  public void closeExpiredSessions(LocalDateTime now) {
    List<QrSessionModel> activeSessions = qrSessionRepository.findAllByStatusIgnoreCase("active");

    for (QrSessionModel session : activeSessions) {
      if (session.getEndsAt() != null && now.isAfter(session.getEndsAt())) {
        session.setStatus("expired");
        session.setStoppedAt(now);
        if (session.getValidUntil() == null
            || session.getValidUntil().isAfter(session.getEndsAt())) {
          session.setValidUntil(session.getEndsAt());
        }
        qrSessionRepository.save(session);
      }
    }
  }

  @Transactional
  public QrSessionModel ensureTodayQrSession(
      LocalDateTime now, OfficerModel createdBy, String location) {
    closeExpiredSessions(now);
    ShiftWindow window = getCurrentShiftWindow(now);

    if (window == null) {
      return null;
    }

    QrSessionModel session =
        qrSessionRepository
            .findTopBySessionDateAndShiftTypeOrderByIdDesc(window.date(), window.shiftType())
            .orElse(null);

    if (session == null) {
      session =
          QrSessionModel.builder()
              .token(generateSessionToken())
              .status("active")
              .location(location)
              .validUntil(window.endsAt())
              .qrRefreshInterval(
                  (int) java.time.Duration.between(window.startsAt(), window.endsAt()).getSeconds())
              .createdBy(createdBy)
              .sessionDate(window.date())
              .shiftType(window.shiftType())
              .startsAt(window.startsAt())
              .endsAt(window.endsAt())
              .systemGenerated(true)
              .startedAt(window.startsAt())
              .build();
      deactivateOtherActiveSessions(session, now);
      return qrSessionRepository.save(session);
    }

    if (!"active".equalsIgnoreCase(session.getStatus())) {
      session.setStatus("active");
      session.setStoppedAt(null);
    }
    session.setSessionDate(window.date());
    session.setShiftType(window.shiftType());
    session.setStartsAt(window.startsAt());
    session.setEndsAt(window.endsAt());
    session.setStartedAt(window.startsAt());
    session.setValidUntil(window.endsAt());
    if (session.getSystemGenerated() == null) {
      session.setSystemGenerated(true);
    }
    if (session.getLocation() == null || session.getLocation().isBlank()) {
      session.setLocation(location);
    }
    deactivateOtherActiveSessions(session, now);
    return qrSessionRepository.save(session);
  }

  public boolean isScanAllowed(LocalDateTime now, QrSessionModel session) {
    if (session == null
        || session.getStatus() == null
        || !"active".equalsIgnoreCase(session.getStatus())) {
      return false;
    }

    if (session.getStartsAt() != null && now.isBefore(session.getStartsAt())) {
      return false;
    }

    if (session.getEndsAt() != null && now.isAfter(session.getEndsAt())) {
      return false;
    }

    if (session.getValidUntil() != null && !session.getValidUntil().isAfter(now)) {
      return false;
    }

    return true;
  }

  public String activeMessageForWindow(ShiftWindow window) {
    if (window == null) {
      return "No active QR session";
    }
    return switch (window.shiftType().toLowerCase(Locale.ROOT)) {
      case "morning" -> "Morning session active";
      case "afternoon" -> "Afternoon session active";
      default -> "No active QR session";
    };
  }

  private void deactivateOtherActiveSessions(QrSessionModel keepSession, LocalDateTime now) {
    List<QrSessionModel> activeSessions = qrSessionRepository.findAllByStatusIgnoreCase("active");
    for (QrSessionModel activeSession : activeSessions) {
      if (keepSession.getId() != null && keepSession.getId().equals(activeSession.getId())) {
        continue;
      }
      if (keepSession.getToken().equals(activeSession.getToken())) {
        continue;
      }
      activeSession.setStatus("expired");
      activeSession.setStoppedAt(now);
      qrSessionRepository.save(activeSession);
    }
  }

  private String generateSessionToken() {
    return "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }

  public record ShiftWindow(
      String shiftType, String shiftLabel, LocalDate date, LocalTime startTime, LocalTime endTime) {
    public LocalDateTime startsAt() {
      return LocalDateTime.of(date, startTime);
    }

    public LocalDateTime endsAt() {
      return LocalDateTime.of(date, endTime);
    }
  }
}
