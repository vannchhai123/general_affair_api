package com.norton.backend.repositories;

import com.norton.backend.models.DocumentLogModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentLogRepository extends JpaRepository<DocumentLogModel, Long> {
  List<DocumentLogModel> findByDocumentId(Long documentId);
}
