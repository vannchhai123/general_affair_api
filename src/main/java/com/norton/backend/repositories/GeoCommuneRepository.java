package com.norton.backend.repositories;

import com.norton.backend.models.GeoCommuneModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeoCommuneRepository extends JpaRepository<GeoCommuneModel, Long> {
  Optional<GeoCommuneModel> findByCode(String code);
}
