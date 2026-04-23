package com.norton.backend.services.qr;

import com.norton.backend.dto.request.CreateQrSessionCheckInRequest;
import com.norton.backend.dto.request.CreateQrSessionRequest;
import com.norton.backend.dto.request.UpdateQrSessionRequest;
import com.norton.backend.dto.responses.qr.CreateQrSessionResponse;
import com.norton.backend.dto.responses.qr.EndQrSessionResponse;
import com.norton.backend.dto.responses.qr.QrSessionCheckInResponse;
import com.norton.backend.dto.responses.qr.QrSessionDetailsResponse;
import com.norton.backend.dto.responses.qr.QrSessionKioskTokenResponse;
import com.norton.backend.dto.responses.qr.QrSessionStatsResponse;
import com.norton.backend.dto.responses.qr.UpdateQrSessionResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.GoneException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.QrSessionCheckInModel;
import com.norton.backend.models.QrSessionLogModel;
import com.norton.backend.models.QrSessionModel;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.QrSessionCheckInRepository;
import com.norton.backend.repositories.QrSessionLogRepository;
import com.norton.backend.repositories.QrSessionRepository;
import com.norton.backend.security.JwtService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrSessionServiceImpl implements QrSessionService {
  private static final int KIOSK_QR_EXPIRES_IN_SECONDS = 15 * 60;
  private static final Pattern SESSION_ID_PATTERN = Pattern.compile("^sess_[A-Za-z0-9]{12}$");

  private final QrSessionRepository qrSessionRepository;
  private final OfficerRepository officerRepository;
  private final QrSessionCheckInRepository qrSessionCheckInRepository;
  private final QrSessionLogRepository qrSessionLogRepository;
  private final JwtService jwtService;
  private final QrSessionLifecycleService qrSessionLifecycleService;

  @Override
  @Transactional
  public CreateQrSessionResponse createQrSession(CreateQrSessionRequest request) {
    qrSessionLifecycleService.closeExpiredSessions(qrSessionLifecycleService.now());

    OfficerModel createdBy =
        officerRepository
            .findById(request.getCreatedBy())
            .orElseThrow(
                () -> new ResourceNotFoundException("Officer", "id", request.getCreatedBy()));

    String sessionToken = generateSessionToken();
    LocalDateTime startedAt = qrSessionLifecycleService.now();
    LocalDateTime validUntil = startedAt.plusSeconds(request.getDurationSeconds());

    QrSessionModel qrSession =
        QrSessionModel.builder()
            .token(sessionToken)
            .status("active")
            .location(request.getLocation())
            .validUntil(validUntil)
            .qrRefreshInterval(request.getDurationSeconds())
            .createdBy(createdBy)
            .sessionDate(startedAt.toLocalDate())
            .shiftType("manual")
            .startsAt(startedAt)
            .endsAt(validUntil)
            .systemGenerated(false)
            .startedAt(startedAt)
            .build();

    QrSessionModel savedSession = qrSessionRepository.save(qrSession);
    Instant createdAt =
        savedSession.getCreatedAt() != null ? savedSession.getCreatedAt() : toInstant(startedAt);

    return CreateQrSessionResponse.builder()
        .id(savedSession.getToken())
        .status(savedSession.getStatus())
        .createdAt(createdAt)
        .expiresAt(toInstant(savedSession.getValidUntil()))
        .location(savedSession.getLocation())
        .build();
  }

  @Override
  @Transactional
  public QrSessionKioskTokenResponse getKioskQrToken(String sessionId) {
    qrSessionLifecycleService.closeExpiredSessions(qrSessionLifecycleService.now());

    String normalizedSessionId = validateSessionId(sessionId);
    QrSessionModel session = getSessionByToken(normalizedSessionId);
    validateKioskSession(session, qrSessionLifecycleService.now());

    return QrSessionKioskTokenResponse.builder()
        .token(
            jwtService.generateQrSessionKioskToken(
                session.getToken(), Duration.ofSeconds(KIOSK_QR_EXPIRES_IN_SECONDS)))
        .expiresIn(KIOSK_QR_EXPIRES_IN_SECONDS)
        .sessionId(session.getToken())
        .status(session.getStatus())
        .message(resolveSessionActiveMessage(session))
        .shiftType(session.getShiftType())
        .startsAt(toInstant(session.getStartsAt()))
        .endsAt(toInstant(session.getEndsAt()))
        .build();
  }

  @Override
  @Transactional
  public QrSessionKioskTokenResponse getCurrentKioskQrToken() {
    LocalDateTime now = qrSessionLifecycleService.now();
    QrSessionModel session =
        qrSessionLifecycleService.ensureTodayQrSession(
            now, resolveCurrentOfficer(), "Main Entrance");

    if (session == null || !qrSessionLifecycleService.isScanAllowed(now, session)) {
      return QrSessionKioskTokenResponse.builder()
          .token(null)
          .expiresIn(0)
          .sessionId(null)
          .status("inactive")
          .message("No active QR session")
          .build();
    }

    return QrSessionKioskTokenResponse.builder()
        .token(
            jwtService.generateQrSessionKioskToken(
                session.getToken(), Duration.ofSeconds(KIOSK_QR_EXPIRES_IN_SECONDS)))
        .expiresIn(KIOSK_QR_EXPIRES_IN_SECONDS)
        .sessionId(session.getToken())
        .status(session.getStatus())
        .message(resolveSessionActiveMessage(session))
        .shiftType(session.getShiftType())
        .startsAt(toInstant(session.getStartsAt()))
        .endsAt(toInstant(session.getEndsAt()))
        .build();
  }

  @Override
  @Transactional
  public QrSessionDetailsResponse getCurrentQrSession() {
    LocalDateTime now = qrSessionLifecycleService.now();
    QrSessionModel session =
        qrSessionLifecycleService.ensureTodayQrSession(
            now, resolveCurrentOfficer(), "Main Entrance");

    if (session == null || !qrSessionLifecycleService.isScanAllowed(now, session)) {
      return QrSessionDetailsResponse.builder()
          .status("inactive")
          .active(false)
          .message("No active QR session")
          .sessionDate(now.toLocalDate())
          .build();
    }

    return toDetailsResponse(session, resolveSessionActiveMessage(session), true);
  }

  @Override
  @Transactional
  public List<QrSessionDetailsResponse> getTodayQrSessions() {
    LocalDateTime now = qrSessionLifecycleService.now();
    qrSessionLifecycleService.closeExpiredSessions(now);

    return qrSessionRepository.findAllBySessionDateOrderByStartsAtAsc(now.toLocalDate()).stream()
        .map(
            session ->
                toDetailsResponse(
                    session, resolveSessionStateMessage(session, now), isActive(session, now)))
        .toList();
  }

  @Override
  @Transactional
  public QrSessionDetailsResponse getQrSession(String id) {
    qrSessionLifecycleService.closeExpiredSessions(qrSessionLifecycleService.now());
    QrSessionModel session = getSessionByToken(id);
    return toDetailsResponse(
        session,
        resolveSessionStateMessage(session, qrSessionLifecycleService.now()),
        isActive(session, qrSessionLifecycleService.now()));
  }

  @Override
  @Transactional
  public UpdateQrSessionResponse updateQrSession(String id, UpdateQrSessionRequest request) {
    qrSessionLifecycleService.closeExpiredSessions(qrSessionLifecycleService.now());
    QrSessionModel session = getSessionByToken(id);
    String action = request.getAction().trim().toLowerCase(Locale.ROOT);
    LocalDateTime now = qrSessionLifecycleService.now();

    switch (action) {
      case "pause" -> session.setStatus("paused");
      case "resume" -> {
        session.setStatus("active");
        session.setStoppedAt(null);
      }
      case "stop" -> {
        session.setStatus("stopped");
        session.setStoppedAt(now);
      }
      case "regenerate" -> {
        session.setToken(generateSessionToken());
        session.setStatus("active");
        session.setSessionDate(now.toLocalDate());
        session.setShiftType(session.getShiftType() != null ? session.getShiftType() : "manual");
        session.setStartsAt(now);
        session.setEndsAt(now.plusSeconds(resolveDurationSeconds(session)));
        if (session.getSystemGenerated() == null) {
          session.setSystemGenerated(true);
        }
        session.setStartedAt(now);
        session.setStoppedAt(null);
        session.setValidUntil(now.plusSeconds(resolveDurationSeconds(session)));
      }
      default ->
          throw new BadRequestException(
              "Invalid action. Allowed actions: pause, resume, stop, regenerate");
    }

    QrSessionModel updatedSession = qrSessionRepository.save(session);
    logAction(updatedSession, action);

    return UpdateQrSessionResponse.builder()
        .id(updatedSession.getToken())
        .status(updatedSession.getStatus())
        .updatedAt(updatedSession.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional
  public List<QrSessionCheckInResponse> getQrSessionCheckIns(String id) {
    qrSessionLifecycleService.closeExpiredSessions(qrSessionLifecycleService.now());
    getSessionByToken(id);
    return qrSessionCheckInRepository.findAllByQrSessionTokenWithOfficer(id).stream()
        .map(checkIn -> toCheckInResponse(checkIn, null))
        .toList();
  }

  @Override
  @Transactional
  public QrSessionCheckInResponse createQrSessionCheckIn(
      String id, CreateQrSessionCheckInRequest request) {
    qrSessionLifecycleService.closeExpiredSessions(qrSessionLifecycleService.now());
    QrSessionModel session = getSessionByToken(id);
    validateCheckInSession(session, qrSessionLifecycleService.now());

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(request.getEmployeeId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Officer", "id", request.getEmployeeId()));

    String normalizedAction = normalizeCheckInAction(request.getAction());
    String status = mapStatusFromAction(normalizedAction, request.getTimestamp());

    QrSessionCheckInModel checkIn =
        QrSessionCheckInModel.builder()
            .qrSession(session)
            .officer(officer)
            .action(normalizedAction)
            .status(status)
            .scannedAt(LocalDateTime.ofInstant(request.getTimestamp(), ZoneOffset.UTC))
            .build();

    QrSessionCheckInModel savedCheckIn = qrSessionCheckInRepository.save(checkIn);

    return toCheckInResponse(savedCheckIn, buildSuccessMessage(normalizedAction));
  }

  @Override
  @Transactional
  public QrSessionStatsResponse getQrSessionStats(String id) {
    qrSessionLifecycleService.closeExpiredSessions(qrSessionLifecycleService.now());
    QrSessionModel session = getSessionByToken(id);
    SessionStats stats = buildSessionStats(session.getToken());

    return QrSessionStatsResponse.builder()
        .sessionId(session.getToken())
        .totalScans(stats.totalScans())
        .checkedIn(stats.checkedIn())
        .checkedOut(stats.checkedOut())
        .late(stats.late())
        .status(session.getStatus())
        .startedAt(toInstant(session.getStartedAt()))
        .lastScanAt(stats.lastScanAt())
        .build();
  }

  @Override
  @Transactional
  public EndQrSessionResponse endQrSession(String id) {
    QrSessionModel session = getSessionByToken(id);
    session.setStatus("stopped");
    session.setStoppedAt(qrSessionLifecycleService.now());

    QrSessionModel updatedSession = qrSessionRepository.save(session);
    logAction(updatedSession, "end");

    SessionStats stats = buildSessionStats(updatedSession.getToken());

    return EndQrSessionResponse.builder()
        .message("Session ended successfully")
        .id(updatedSession.getToken())
        .finalStats(
            EndQrSessionResponse.FinalStats.builder()
                .totalScans(stats.totalScans())
                .checkedIn(stats.checkedIn())
                .checkedOut(stats.checkedOut())
                .late(stats.late())
                .build())
        .build();
  }

  private String generateSessionToken() {
    return "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }

  private String validateSessionId(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) {
      throw new BadRequestException("sessionId is required");
    }

    String normalizedSessionId = sessionId.trim();
    if (!SESSION_ID_PATTERN.matcher(normalizedSessionId).matches()) {
      throw new BadRequestException("Invalid sessionId format");
    }

    return normalizedSessionId;
  }

  private QrSessionModel getSessionByToken(String token) {
    return qrSessionRepository
        .findByTokenWithCreatedBy(token)
        .orElseThrow(() -> new ResourceNotFoundException("QrSession", "id", token));
  }

  private Instant toCreatedAt(QrSessionModel session) {
    return session.getCreatedAt() != null
        ? session.getCreatedAt()
        : toInstant(session.getStartedAt());
  }

  private Instant toInstant(LocalDateTime dateTime) {
    return dateTime != null
        ? dateTime.atZone(qrSessionLifecycleService.resolveZoneId()).toInstant()
        : null;
  }

  private int resolveDurationSeconds(QrSessionModel session) {
    if (session.getQrRefreshInterval() != null && session.getQrRefreshInterval() > 0) {
      return session.getQrRefreshInterval();
    }
    return 60;
  }

  private void logAction(QrSessionModel session, String action) {
    qrSessionLogRepository.save(
        QrSessionLogModel.builder()
            .qrSession(session)
            .action(action)
            .performedBy(resolveCurrentOfficer())
            .details("QR session " + action)
            .build());
  }

  private QrSessionCheckInResponse toCheckInResponse(
      QrSessionCheckInModel checkIn, String message) {
    OfficerModel officer = checkIn.getOfficer();
    return QrSessionCheckInResponse.builder()
        .id(checkIn.getId())
        .officerName(officer.getFirstName() + " " + officer.getLastName())
        .officerCode(officer.getOfficerCode())
        .department(officer.getPosition().getDepartment().getName())
        .status(checkIn.getStatus())
        .scannedAt(toInstant(checkIn.getScannedAt()))
        .message(message)
        .build();
  }

  private String normalizeCheckInAction(String action) {
    String normalized = action.trim().toLowerCase(Locale.ROOT);
    if (!normalized.equals("check-in") && !normalized.equals("check-out")) {
      throw new BadRequestException("Invalid action. Allowed actions: check-in, check-out");
    }
    return normalized;
  }

  private String mapStatusFromAction(String action, Instant timestamp) {
    if ("check-out".equals(action)) {
      return "checked-out";
    }

    Instant eightAm =
        timestamp.atZone(ZoneOffset.UTC).toLocalDate().atTime(8, 0).toInstant(ZoneOffset.UTC);
    if (timestamp.isAfter(eightAm)) {
      return "late";
    }
    return "checked-in";
  }

  private String buildSuccessMessage(String action) {
    return "check-in".equals(action) ? "Check-in successful" : "Check-out successful";
  }

  private void validateCheckInSession(QrSessionModel session, LocalDateTime now) {
    if (!"active".equalsIgnoreCase(session.getStatus())) {
      throw new BadRequestException("No active QR session");
    }

    if (!qrSessionLifecycleService.isScanAllowed(now, session)) {
      if (session.getEndsAt() != null && now.isAfter(session.getEndsAt())) {
        throw new BadRequestException("QR session expired");
      }
      throw new BadRequestException("No active QR session");
    }
  }

  private void validateKioskSession(QrSessionModel session, LocalDateTime now) {
    if (session.getStatus() == null || !"active".equalsIgnoreCase(session.getStatus())) {
      throw new GoneException("No active QR session");
    }

    if (!qrSessionLifecycleService.isScanAllowed(now, session)) {
      throw new GoneException("QR session expired");
    }
  }

  private OfficerModel resolveCurrentOfficer() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof com.norton.backend.models.UserModel currentUser)) {
      return null;
    }

    return officerRepository.findByUserIdWithPosition(currentUser.getId()).orElse(null);
  }

  private SessionStats buildSessionStats(String token) {
    List<QrSessionCheckInModel> checkIns =
        qrSessionCheckInRepository.findAllByQrSessionTokenWithOfficer(token);
    long checkedIn =
        checkIns.stream()
            .filter(checkIn -> "checked-in".equalsIgnoreCase(checkIn.getStatus()))
            .count();
    long checkedOut =
        checkIns.stream()
            .filter(checkIn -> "checked-out".equalsIgnoreCase(checkIn.getStatus()))
            .count();
    long late =
        checkIns.stream().filter(checkIn -> "late".equalsIgnoreCase(checkIn.getStatus())).count();
    Instant lastScanAt =
        checkIns.stream()
            .map(QrSessionCheckInModel::getScannedAt)
            .filter(java.util.Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .map(this::toInstant)
            .orElse(null);

    return new SessionStats(checkIns.size(), checkedIn, checkedOut, late, lastScanAt);
  }

  private record SessionStats(
      long totalScans, long checkedIn, long checkedOut, long late, Instant lastScanAt) {}

  private QrSessionDetailsResponse toDetailsResponse(
      QrSessionModel session, String message, boolean active) {
    return QrSessionDetailsResponse.builder()
        .id(session.getToken())
        .status(session.getStatus())
        .createdBy(session.getCreatedBy() != null ? session.getCreatedBy().getId() : null)
        .createdAt(toCreatedAt(session))
        .expiresAt(toInstant(session.getValidUntil()))
        .qrToken("attendance://" + session.getToken())
        .scanCount(qrSessionCheckInRepository.countByQrSessionToken(session.getToken()))
        .sessionDate(session.getSessionDate())
        .shiftType(session.getShiftType())
        .startsAt(toInstant(session.getStartsAt()))
        .endsAt(toInstant(session.getEndsAt()))
        .systemGenerated(session.getSystemGenerated())
        .message(message)
        .active(active)
        .location(session.getLocation())
        .build();
  }

  private boolean isActive(QrSessionModel session, LocalDateTime now) {
    return qrSessionLifecycleService.isScanAllowed(now, session);
  }

  private String resolveSessionActiveMessage(QrSessionModel session) {
    if (session.getShiftType() == null) {
      return "No active QR session";
    }
    return switch (session.getShiftType().toLowerCase(Locale.ROOT)) {
      case "morning" -> "Morning session active";
      case "afternoon" -> "Afternoon session active";
      default -> "QR session active";
    };
  }

  private String resolveSessionStateMessage(QrSessionModel session, LocalDateTime now) {
    if (isActive(session, now)) {
      return resolveSessionActiveMessage(session);
    }

    if (session.getEndsAt() != null && now.isAfter(session.getEndsAt())) {
      return "QR session expired";
    }

    return "No active QR session";
  }
}
