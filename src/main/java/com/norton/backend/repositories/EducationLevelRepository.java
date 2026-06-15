package com.norton.backend.repositories;

import com.norton.backend.models.EducationLevelModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationLevelRepository extends JpaRepository<EducationLevelModel, Long> {
  Optional<EducationLevelModel> findByNameIgnoreCase(String name);
}
