package com.norton.backend.dto.responses.qr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrSessionDetailsResponse {
  private String id;
  private String status;

  @JsonProperty("created_by")
  private Long createdBy;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("expires_at")
  private Instant expiresAt;

  @JsonProperty("qr_token")
  private String qrToken;

  @JsonProperty("scan_count")
  private long scanCount;

  private String location;
}
