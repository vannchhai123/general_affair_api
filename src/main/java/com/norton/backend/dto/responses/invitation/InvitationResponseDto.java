package com.norton.backend.dto.responses.invitation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponseDto {

  private String message;
  private Long invitationId;
  private Long officerId;
  private String status;
  private String rejectionReason;
}
