package com.norton.backend.repositories;

import com.norton.backend.models.DocumentTagModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTagRepository extends JpaRepository<DocumentTagModel, Long> {
  Optional<DocumentTagModel> findByName(String name);
}
