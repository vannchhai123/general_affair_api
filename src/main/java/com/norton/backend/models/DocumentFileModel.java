package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "document_files",
    indexes = {
      @Index(name = "idx_doc_files_doc_id", columnList = "document_id"),
      @Index(name = "idx_doc_files_upload_img", columnList = "upload_image_id"),
      @Index(name = "idx_doc_files_uploaded_by", columnList = "uploaded_by")
    })
public class DocumentFileModel extends BaseIdModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_id", nullable = false)
  private DocumentModel document;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "upload_image_id")
  private UploadImageModel uploadImage;

  @Size(max = 500, message = "File name must not exceed 500 characters")
  @Column(name = "file_name", length = 500)
  private String fileName;

  @Size(max = 2048, message = "File path must not exceed 2048 characters")
  @Column(name = "file_path", length = 2048)
  private String filePath;

  @Size(max = 100, message = "Mime type must not exceed 100 characters")
  @Column(name = "mime_type", length = 100)
  private String mimeType;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "is_primary")
  private Boolean isPrimary;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by")
  private OfficerModel uploadedBy;
}
