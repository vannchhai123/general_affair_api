package com.norton.backend.services.qr;

import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.QrSessionModel;
import com.norton.backend.repositories.QrSessionRepository;
import com.norton.backend.services.shift.ShiftResolutionService;
import com.norton.backend.services.shift.ShiftResolutionService.ShiftWindow;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrSessionLifecycleService {
  private final QrSessionRepository qrSessionRepository;
  private final ShiftResolutionService shiftResolutionService;

  @Value("${attendance.scan.timezone:Asia/Phnom_Penh}")
  private String sessionTimezone;

  public ZoneId resolveZoneId() {
    return ZoneId.of(sessionTimezone);
  }

  public LocalDateTime now() {
    return LocalDateTime.now(resolveZoneId());
  }

  public ShiftWindow getCurrentShiftWindow(LocalDateTime now) {
    return shiftResolutionService.resolveActiveShift(now).orElse(null);
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
            .findTopBySessionDateAndShiftTypeOrderByIdDesc(
                window.shiftDate(), shiftResolutionService.shiftType(window.shift()))
            .orElse(null);

    if (session == null) {
      session =
          QrSessionModel.builder()
              .token(generateSessionToken())
              .status("active")
              .location(location)
              .validUntil(window.checkOutClosesAt())
              .qrRefreshInterval(
                  (int)
                      java.time.Duration.between(window.checkInOpensAt(), window.checkOutClosesAt())
                          .getSeconds())
              .createdBy(createdBy)
              .sessionDate(window.shiftDate())
              .shiftType(shiftResolutionService.shiftType(window.shift()))
              .startsAt(window.checkInOpensAt())
              .endsAt(window.checkOutClosesAt())
              .systemGenerated(true)
              .startedAt(window.checkInOpensAt())
              .build();
      deactivateOtherActiveSessions(session, now);
      return qrSessionRepository.save(session);
    }

    if (!"active".equalsIgnoreCase(session.getStatus())) {
      session.setStatus("active");
      session.setStoppedAt(null);
    }
    session.setSessionDate(window.shiftDate());
    session.setShiftType(shiftResolutionService.shiftType(window.shift()));
    session.setStartsAt(window.checkInOpensAt());
    session.setEndsAt(window.checkOutClosesAt());
    session.setStartedAt(window.checkInOpensAt());
    session.setValidUntil(window.checkOutClosesAt());
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
    return shiftResolutionService.shiftLabel(window.shift()) + " session active";
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
}
