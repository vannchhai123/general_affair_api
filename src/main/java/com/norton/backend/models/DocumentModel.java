package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "documents",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_docs_number_type",
          columnNames = {"document_number", "document_type_id"})
    },
    indexes = {
      @Index(name = "idx_docs_uuid", columnList = "uuid"),
      @Index(name = "idx_docs_doc_number", columnList = "document_number"),
      @Index(name = "idx_docs_direction", columnList = "direction"),
      @Index(name = "idx_docs_doc_type", columnList = "document_type_id"),
      @Index(name = "idx_docs_sender_org", columnList = "sender_organization_id"),
      @Index(name = "idx_docs_receiver_org", columnList = "receiver_organization_id"),
      @Index(name = "idx_docs_doc_date", columnList = "document_date"),
      @Index(name = "idx_docs_received_date", columnList = "received_date"),
      @Index(name = "idx_docs_created_by", columnList = "created_by"),
      @Index(name = "idx_docs_status", columnList = "status"),
      @Index(name = "idx_docs_dir_date", columnList = "direction, document_date"),
      @Index(name = "idx_docs_type_date", columnList = "document_type_id, document_date"),
      @Index(name = "idx_docs_sender_date", columnList = "sender_organization_id, document_date"),
      @Index(
          name = "idx_docs_receiver_date",
          columnList = "receiver_organization_id, document_date"),
      @Index(name = "idx_docs_status_date", columnList = "status, document_date")
    })
public class DocumentModel extends BaseIdModel {

  @Column(nullable = false, unique = true)
  private UUID uuid;

  @NotBlank(message = "Direction is required")
  @Size(max = 10, message = "Direction must not exceed 10 characters")
  @Column(nullable = false, length = 10)
  private String direction; // e.g. "INCOMING", "OUTGOING"

  @NotNull(message = "Document type is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_type_id", nullable = false)
  private DocumentTypeModel documentType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_organization_id")
  private OrganizationModel senderOrganization;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_organization_id")
  private OrganizationModel receiverOrganization;

  @Size(max = 100, message = "Document number must not exceed 100 characters")
  @Column(name = "document_number", length = 100)
  private String documentNumber;

  @NotNull(message = "Document date is required")
  @Column(name = "document_date", nullable = false)
  private LocalDate documentDate;

  @Column(name = "received_date")
  private LocalDate receivedDate;

  @NotBlank(message = "Subject is required")
  @Size(max = 500, message = "Subject must not exceed 500 characters")
  @Column(nullable = false, length = 500)
  private String subject;

  @Column(columnDefinition = "TEXT")
  private String summary;

  @Size(max = 30, message = "Confidentiality must not exceed 30 characters")
  @Column(length = 30)
  private String confidentiality;

  @Size(max = 30, message = "Priority must not exceed 30 characters")
  @Column(length = 30)
  private String priority;

  @Size(max = 30, message = "Status must not exceed 30 characters")
  @Column(length = 30)
  private String status;

  @Column(columnDefinition = "TEXT")
  private String remarks;

  @NotNull(message = "Created by is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  private OfficerModel createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private OfficerModel updatedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by")
  private OfficerModel deletedBy;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DocumentFileModel> files = new ArrayList<>();

  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DocumentLogModel> logs = new ArrayList<>();

  @PrePersist
  public void generateUuid() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID();
    }
  }
}
