package com.norton.backend.dto.request.invitation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class CreateInvitationRequest {

  @JsonProperty("title")
  @JsonAlias({"title", "subject"})
  private String title;

  private String description;

  @JsonProperty("presidedBy")
  @JsonAlias({"presidedBy", "organization"})
  private String presidedBy;

  @JsonProperty("eventDate")
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonAlias({"eventDate", "date"})
  private LocalDate eventDate;

  @JsonProperty("eventTime")
  @JsonFormat(pattern = "[HH:mm:ss][HH:mm]")
  @JsonAlias({"eventTime", "time"})
  private LocalTime eventTime;

  private String location;

  private String type;

  private String status;

  @JsonProperty("imageIds")
  @JsonAlias({"imageIds", "image_ids"})
  private List<Long> imageIds;

  @JsonProperty("participant_ids")
  @JsonAlias({"participantIds", "participant_ids", "officers"})
  private List<Long> participantIds;
}
