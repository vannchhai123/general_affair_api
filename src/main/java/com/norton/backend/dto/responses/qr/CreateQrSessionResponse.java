package com.norton.backend.dto.responses.qr;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateQrSessionResponse {
  private String id;
  private String status;
  private Instant createdAt;
  private Instant expiresAt;
  private String location;
}
