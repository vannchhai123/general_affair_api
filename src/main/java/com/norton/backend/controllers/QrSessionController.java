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
  public ResponseEntity<CreateQrSessionResponse> createQrSession(
      @Valid @RequestBody CreateQrSessionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(qrSessionService.createQrSession(request));
  }

  @GetMapping("/{id}")
  public ResponseEntity<QrSessionDetailsResponse> getQrSession(@PathVariable String id) {
    return ResponseEntity.ok(qrSessionService.getQrSession(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UpdateQrSessionResponse> updateQrSession(
      @PathVariable String id, @Valid @RequestBody UpdateQrSessionRequest request) {
    return ResponseEntity.ok(qrSessionService.updateQrSession(id, request));
  }

  @GetMapping("/{id}/checkins")
  public ResponseEntity<List<QrSessionCheckInResponse>> getQrSessionCheckIns(
      @PathVariable String id) {
    return ResponseEntity.ok(qrSessionService.getQrSessionCheckIns(id));
  }

  @PostMapping("/{id}/checkins")
  public ResponseEntity<QrSessionCheckInResponse> createQrSessionCheckIn(
      @PathVariable String id, @Valid @RequestBody CreateQrSessionCheckInRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(qrSessionService.createQrSessionCheckIn(id, request));
  }

  @GetMapping("/{id}/stats")
  public ResponseEntity<QrSessionStatsResponse> getQrSessionStats(@PathVariable String id) {
    return ResponseEntity.ok(qrSessionService.getQrSessionStats(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<EndQrSessionResponse> endQrSession(@PathVariable String id) {
    return ResponseEntity.ok(qrSessionService.endQrSession(id));
  }
}
