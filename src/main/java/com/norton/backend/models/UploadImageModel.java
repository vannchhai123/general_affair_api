package com.norton.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "upload_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadImageModel extends BaseIdModel {

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "url", nullable = false, length = 2048)
  private String url;
}
