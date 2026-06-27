package com.norton.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "invitation_images",
    uniqueConstraints = @UniqueConstraint(columnNames = {"invitation_id", "upload_image_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationImageModel extends BaseIdModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invitation_id", nullable = false)
  private InvitationModel invitation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "upload_image_id", nullable = false)
  private UploadImageModel uploadImage;
}
