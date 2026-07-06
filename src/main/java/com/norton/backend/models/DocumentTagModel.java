package com.norton.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "document_tags",
    indexes = {@Index(name = "idx_doc_tags_name", columnList = "name")})
public class DocumentTagModel extends BaseIdModel {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must not exceed 100 characters")
  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
  @Builder.Default
  private List<DocumentModel> documents = new ArrayList<>();
}
