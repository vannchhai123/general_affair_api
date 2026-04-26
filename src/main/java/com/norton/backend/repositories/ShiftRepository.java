package com.norton.backend.repositories;

import com.norton.backend.models.ShiftModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository
    extends JpaRepository<ShiftModel, Long>, JpaSpecificationExecutor<ShiftModel> {
  java.util.Optional<ShiftModel> findByName(String name);

  boolean existsByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
