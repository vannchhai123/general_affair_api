package com.norton.backend.dto.responses.qr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateQrSessionResponse {
  private String id;
  private String status;

  @JsonProperty("updated_at")
  private Instant updatedAt;
}
