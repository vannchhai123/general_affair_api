package com.norton.backend.dto.responses.qr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QrSessionKioskTokenResponse {
  private String token;
  private int expiresIn;
}
