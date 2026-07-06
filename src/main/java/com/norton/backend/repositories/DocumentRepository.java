package com.norton.backend.repositories;

import com.norton.backend.models.DocumentModel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentModel, Long> {
  Optional<DocumentModel> findByUuid(UUID uuid);

  Optional<DocumentModel> findByDocumentNumber(String documentNumber);
}
