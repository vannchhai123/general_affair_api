package com.norton.backend.dto.responses.invitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisplayInvitationResponse {

  private Long id;

  @JsonProperty("subject")
  private String title;

  private String description;

  @JsonProperty("organization")
  private String presidedBy;

  @JsonProperty("date")
  private String eventDate;

  @JsonProperty("time")
  private String eventTime;

  private String location;

  private List<String> imageUrls;
  private String type;
  private String status;

  @JsonProperty("myResponse")
  private ParticipantResponseDto myResponse;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ParticipantResponseDto {
    private String status;

    @JsonProperty("rejection_reason")
    private String rejectionReason;
  }
}
