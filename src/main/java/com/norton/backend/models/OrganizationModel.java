package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "organizations",
    indexes = {
      @Index(name = "idx_orgs_uuid", columnList = "uuid"),
      @Index(name = "idx_orgs_name", columnList = "name"),
      @Index(name = "idx_orgs_short_name", columnList = "short_name"),
      @Index(name = "idx_orgs_org_type", columnList = "organization_type"),
      @Index(name = "idx_orgs_status", columnList = "status")
    })
public class OrganizationModel extends BaseIdModel {

  @Column(nullable = false, unique = true)
  private UUID uuid;

  @NotBlank(message = "Name is required")
  @Size(max = 255, message = "Name must not exceed 255 characters")
  @Column(nullable = false, unique = true, length = 255)
  private String name;

  @Size(max = 100, message = "Short name must not exceed 100 characters")
  @Column(name = "short_name", length = 100)
  private String shortName;

  @Size(max = 50, message = "Organization type must not exceed 50 characters")
  @Column(name = "organization_type", length = 50)
  private String organizationType;

  @Size(max = 30, message = "Phone must not exceed 30 characters")
  @Column(length = 30)
  private String phone;

  @Size(max = 150, message = "Email must not exceed 150 characters")
  @Column(length = 150)
  private String email;

  @Size(max = 255, message = "Address must not exceed 255 characters")
  @Column(length = 255)
  private String address;

  @Size(max = 255, message = "Website must not exceed 255 characters")
  @Column(length = 255)
  private String website;

  @Size(max = 30, message = "Status must not exceed 30 characters")
  @Column(length = 30)
  private String status;

  @PrePersist
  public void generateUuid() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID();
    }
  }
}
