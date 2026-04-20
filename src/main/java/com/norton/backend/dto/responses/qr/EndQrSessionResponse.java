package com.norton.backend.dto.responses.qr;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndQrSessionResponse {
  private String message;
  private String id;

  private FinalStats finalStats;

  @Data
  @Builder
  public static class FinalStats {
    private long totalScans;
    private long checkedIn;
    private long checkedOut;
    private long late;
  }
}
