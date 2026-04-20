package com.norton.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateQrSessionRequest {

  @NotBlank(message = "action is required")
  private String action;
}
