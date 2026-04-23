package com.norton.backend.dto.responses.qr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrSessionKioskTokenResponse {
  private String token;
  private int expiresIn;

  @JsonProperty("session_id")
  private String sessionId;

  private String status;
  private String message;

  @JsonProperty("shift_type")
  private String shiftType;

  @JsonProperty("starts_at")
  private Instant startsAt;

  @JsonProperty("ends_at")
  private Instant endsAt;
}
