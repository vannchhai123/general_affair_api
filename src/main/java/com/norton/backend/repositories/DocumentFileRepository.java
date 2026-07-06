package com.norton.backend.repositories;

import com.norton.backend.models.DocumentFileModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFileModel, Long> {
  List<DocumentFileModel> findByDocumentId(Long documentId);
}
