package com.norton.backend.dto.responses.qr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrSessionStatsResponse {
  @JsonProperty("session_id")
  private String sessionId;

  @JsonProperty("total_scans")
  private long totalScans;

  @JsonProperty("checked_in")
  private long checkedIn;

  @JsonProperty("checked_out")
  private long checkedOut;

  private long late;
  private String status;

  @JsonProperty("started_at")
  private Instant startedAt;

  @JsonProperty("last_scan_at")
  private Instant lastScanAt;
}
