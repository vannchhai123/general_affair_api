package com.norton.backend.dto.responses.invitation;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateInvitationResponse {

  private Long id;
  private String title;
  private String description;
  private String presidedBy;
  private String eventDate;
  private String eventTime;
  private String location;
  private List<Long> imageIds;
  private List<String> imageUrls;
  private List<Long> participantIds;
}
