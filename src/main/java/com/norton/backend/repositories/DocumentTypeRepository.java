package com.norton.backend.repositories;

import com.norton.backend.models.DocumentTypeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentTypeModel, Long> {}
