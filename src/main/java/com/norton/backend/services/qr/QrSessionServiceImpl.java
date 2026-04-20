package com.norton.backend.services.qr;

import com.norton.backend.dto.request.CreateQrSessionCheckInRequest;
import com.norton.backend.dto.request.CreateQrSessionRequest;
import com.norton.backend.dto.request.UpdateQrSessionRequest;
import com.norton.backend.dto.responses.qr.CreateQrSessionResponse;
import com.norton.backend.dto.responses.qr.EndQrSessionResponse;
import com.norton.backend.dto.responses.qr.QrSessionCheckInResponse;
import com.norton.backend.dto.responses.qr.QrSessionDetailsResponse;
import com.norton.backend.dto.responses.qr.QrSessionStatsResponse;
import com.norton.backend.dto.responses.qr.UpdateQrSessionResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.QrSessionCheckInModel;
import com.norton.backend.models.QrSessionLogModel;
import com.norton.backend.models.QrSessionModel;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.QrSessionCheckInRepository;
import com.norton.backend.repositories.QrSessionLogRepository;
import com.norton.backend.repositories.QrSessionRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrSessionServiceImpl implements QrSessionService {

  private final QrSessionRepository qrSessionRepository;
  private final OfficerRepository officerRepository;
  private final QrSessionCheckInRepository qrSessionCheckInRepository;
  private final QrSessionLogRepository qrSessionLogRepository;

  @Override
  @Transactional
  public CreateQrSessionResponse createQrSession(CreateQrSessionRequest request) {
    OfficerModel createdBy =
        officerRepository
            .findById(request.getCreatedBy())
            .orElseThrow(
                () -> new ResourceNotFoundException("Officer", "id", request.getCreatedBy()));

    String sessionToken = generateSessionToken();
    LocalDateTime startedAt = LocalDateTime.now(ZoneOffset.UTC);
    LocalDateTime validUntil = startedAt.plusSeconds(request.getDurationSeconds());

    QrSessionModel qrSession =
        QrSessionModel.builder()
            .token(sessionToken)
            .status("active")
            .location(request.getLocation())
            .validUntil(validUntil)
            .qrRefreshInterval(request.getDurationSeconds())
            .createdBy(createdBy)
            .startedAt(startedAt)
            .build();

    QrSessionModel savedSession = qrSessionRepository.save(qrSession);
    Instant createdAt =
        savedSession.getCreatedAt() != null
            ? savedSession.getCreatedAt()
            : startedAt.toInstant(ZoneOffset.UTC);

    return CreateQrSessionResponse.builder()
        .id(savedSession.getToken())
        .qrToken("attendance://" + savedSession.getToken())
        .status(savedSession.getStatus())
        .createdAt(createdAt)
        .expiresAt(savedSession.getValidUntil().toInstant(ZoneOffset.UTC))
        .qrCodeUrl("/api/qr/" + savedSession.getToken() + ".png")
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public QrSessionDetailsResponse getQrSession(String id) {
    QrSessionModel session = getSessionByToken(id);

    return QrSessionDetailsResponse.builder()
        .id(session.getToken())
        .status(session.getStatus())
        .createdBy(session.getCreatedBy() != null ? session.getCreatedBy().getId() : null)
        .createdAt(toCreatedAt(session))
        .expiresAt(toInstant(session.getValidUntil()))
        .qrToken("attendance://" + session.getToken())
        .scanCount(qrSessionCheckInRepository.countByQrSessionToken(session.getToken()))
        .location(session.getLocation())
        .build();
  }

  @Override
  @Transactional
  public UpdateQrSessionResponse updateQrSession(String id, UpdateQrSessionRequest request) {
    QrSessionModel session = getSessionByToken(id);
    String action = request.getAction().trim().toLowerCase(Locale.ROOT);
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

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
  @Transactional(readOnly = true)
  public List<QrSessionCheckInResponse> getQrSessionCheckIns(String id) {
    getSessionByToken(id);
    return qrSessionCheckInRepository.findAllByQrSessionTokenWithOfficer(id).stream()
        .map(checkIn -> toCheckInResponse(checkIn, null))
        .toList();
  }

  @Override
  @Transactional
  public QrSessionCheckInResponse createQrSessionCheckIn(
      String id, CreateQrSessionCheckInRequest request) {
    QrSessionModel session = getSessionByToken(id);
    validateCheckInSession(session);

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
  @Transactional(readOnly = true)
  public QrSessionStatsResponse getQrSessionStats(String id) {
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
    session.setStoppedAt(LocalDateTime.now(ZoneOffset.UTC));

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
    return dateTime != null ? dateTime.toInstant(ZoneOffset.UTC) : null;
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
        .employeeName(officer.getFirstName() + " " + officer.getLastName())
        .employeeCode(officer.getOfficerCode())
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

  private void validateCheckInSession(QrSessionModel session) {
    if (!"active".equalsIgnoreCase(session.getStatus())) {
      throw new BadRequestException("QR session is not active");
    }

    if (session.getValidUntil() != null
        && session.getValidUntil().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
      throw new BadRequestException("QR session has expired");
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
}
