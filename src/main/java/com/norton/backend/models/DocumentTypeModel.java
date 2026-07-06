package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "document_types",
    indexes = {
      @Index(name = "idx_doc_types_name", columnList = "name"),
      @Index(name = "idx_doc_types_code", columnList = "code")
    })
public class DocumentTypeModel extends BaseIdModel {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must not exceed 100 characters")
  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Size(max = 20, message = "Code must not exceed 20 characters")
  @Column(unique = true, length = 20)
  private String code;

  private String description;
}
