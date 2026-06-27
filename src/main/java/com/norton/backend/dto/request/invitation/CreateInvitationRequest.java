package com.norton.backend.dto.request.invitation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class CreateInvitationRequest {

  @NotBlank(message = "title is required")
  private String title;

  @NotBlank(message = "description is required")
  private String description;

  @NotBlank(message = "presidedBy is required")
  private String presidedBy;

  @NotNull(message = "eventDate is required")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate eventDate;

  @NotNull(message = "eventTime is required")
  @JsonFormat(pattern = "HH:mm:ss")
  private LocalTime eventTime;

  @NotBlank(message = "location is required")
  private String location;

  @JsonProperty("imageId")
  private Long imageId;

  @NotNull(message = "participant_ids is required")
  @NotEmpty(message = "participant_ids must not be empty")
  @JsonProperty("participant_ids")
  @JsonAlias({"participantIds", "participant_ids"})
  private List<Long> participantIds;
}
