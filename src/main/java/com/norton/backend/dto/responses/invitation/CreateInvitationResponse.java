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
public class CreateInvitationResponse {

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

  private List<Long> imageIds;
  private List<String> imageUrls;

  @JsonProperty("assigned_officer_ids")
  private List<Long> participantIds;

  @JsonProperty("assigned_officers")
  private List<AssignedOfficerDto> assignedOfficers;

  private String type;
  private String status;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AssignedOfficerDto {
    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("first_name_kh")
    private String firstNameKh;

    @JsonProperty("last_name_kh")
    private String lastNameKh;

    private String department;
    private String position;

    @JsonProperty("officerCode")
    private String officerCode;

    private String status;

    @JsonProperty("rejection_reason")
    private String rejectionReason;
  }
}
