package com.norton.backend.controllers;

import com.norton.backend.dto.responses.qr.QrSessionKioskTokenResponse;
import com.norton.backend.services.qr.QrSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicQrSessionController {
  private final QrSessionService qrSessionService;

  @GetMapping("/api/v1/session/{sessionId}/qr")
  public ResponseEntity<QrSessionKioskTokenResponse> getSessionQr(@PathVariable String sessionId) {
    return ResponseEntity.ok(qrSessionService.getKioskQrToken(sessionId));
  }
}
