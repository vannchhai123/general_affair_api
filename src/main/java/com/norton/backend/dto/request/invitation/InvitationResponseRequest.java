package com.norton.backend.dto.request.invitation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponseRequest {

  private Long officerId;

  @NotBlank(message = "status is required")
  private String status;

  private String rejectionReason;
}
