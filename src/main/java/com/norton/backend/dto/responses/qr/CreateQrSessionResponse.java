package com.norton.backend.dto.responses.qr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateQrSessionResponse {
  private String id;

  @JsonProperty("qr_token")
  private String qrToken;

  private String status;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("expires_at")
  private Instant expiresAt;

  @JsonProperty("qr_code_url")
  private String qrCodeUrl;
}
