package com.norton.backend.controllers;

import com.norton.backend.dto.request.CreateQrSessionCheckInRequest;
import com.norton.backend.dto.request.CreateQrSessionRequest;
import com.norton.backend.dto.request.UpdateQrSessionRequest;
import com.norton.backend.dto.responses.qr.CreateQrSessionResponse;
import com.norton.backend.dto.responses.qr.EndQrSessionResponse;
import com.norton.backend.dto.responses.qr.QrSessionCheckInResponse;
import com.norton.backend.dto.responses.qr.QrSessionDetailsResponse;
import com.norton.backend.dto.responses.qr.QrSessionStatsResponse;
import com.norton.backend.dto.responses.qr.UpdateQrSessionResponse;
import com.norton.backend.services.qr.QrSessionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(QrSessionController.BASE_PATH)
@RequiredArgsConstructor
public class QrSessionController {
  public static final String BASE_PATH = "/api/v1/qr-sessions";

  private final QrSessionService qrSessionService;

  @PostMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_CREATE)")
  public ResponseEntity<CreateQrSessionResponse> createQrSession(
      @Valid @RequestBody CreateQrSessionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(qrSessionService.createQrSession(request));
  }

  @GetMapping("/current")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_VIEW)")
  public ResponseEntity<QrSessionDetailsResponse> getCurrentQrSession() {
    return ResponseEntity.ok(qrSessionService.getCurrentQrSession());
  }

  @GetMapping("/today")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_VIEW)")
  public ResponseEntity<List<QrSessionDetailsResponse>> getTodayQrSessions() {
    return ResponseEntity.ok(qrSessionService.getTodayQrSessions());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_VIEW)")
  public ResponseEntity<QrSessionDetailsResponse> getQrSession(@PathVariable String id) {
    return ResponseEntity.ok(qrSessionService.getQrSession(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_UPDATE)")
  public ResponseEntity<UpdateQrSessionResponse> updateQrSession(
      @PathVariable String id, @Valid @RequestBody UpdateQrSessionRequest request) {
    return ResponseEntity.ok(qrSessionService.updateQrSession(id, request));
  }

  @GetMapping("/{sessionId}/checkins")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_VIEW)")
  public ResponseEntity<List<QrSessionCheckInResponse>> getQrSessionCheckIns(
      @PathVariable String sessionId) {
    return ResponseEntity.ok(qrSessionService.getQrSessionCheckIns(sessionId));
  }

  @PostMapping("/{id}/checkins")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_CHECKIN)")
  public ResponseEntity<QrSessionCheckInResponse> createQrSessionCheckIn(
      @PathVariable String id, @Valid @RequestBody CreateQrSessionCheckInRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(qrSessionService.createQrSessionCheckIn(id, request));
  }

  @GetMapping("/{id}/stats")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_VIEW)")
  public ResponseEntity<QrSessionStatsResponse> getQrSessionStats(@PathVariable String id) {
    return ResponseEntity.ok(qrSessionService.getQrSessionStats(id));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).QR_SESSION_END)")
  public ResponseEntity<EndQrSessionResponse> endQrSession(@PathVariable String id) {
    return ResponseEntity.ok(qrSessionService.endQrSession(id));
  }
}
