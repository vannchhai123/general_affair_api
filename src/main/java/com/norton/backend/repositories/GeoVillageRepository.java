package com.norton.backend.repositories;

import com.norton.backend.models.GeoVillageModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeoVillageRepository extends JpaRepository<GeoVillageModel, Long> {
  Optional<GeoVillageModel> findByCode(String code);
}
