package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationModel extends BaseIdModel {

  @NotBlank(message = "title is required")
  @Column(name = "title", nullable = false, length = 500)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "presided_by", length = 500)
  private String presidedBy;

  @Column(name = "event_date")
  private LocalDate eventDate;

  @Column(name = "event_time")
  private LocalTime eventTime;

  @Column(name = "location", length = 1000)
  private String location;

  @Builder.Default
  @Column(name = "type", length = 50)
  private String type = "incoming";

  @Builder.Default
  @Column(name = "status", length = 50)
  private String status = "pending";

  @Column(name = "image_id")
  private Long imageId;

  @Column(name = "image_url", length = 2048)
  private String imageUrl;

  @Builder.Default
  @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InvitationParticipantModel> participants = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InvitationImageModel> images = new ArrayList<>();

  public void addParticipant(OfficerModel officer) {
    InvitationParticipantModel participant =
        InvitationParticipantModel.builder().officer(officer).invitation(this).build();
    this.participants.add(participant);
  }

  public void addImage(UploadImageModel uploadImage) {
    InvitationImageModel invitationImage =
        InvitationImageModel.builder().uploadImage(uploadImage).invitation(this).build();
    this.images.add(invitationImage);
  }
}
