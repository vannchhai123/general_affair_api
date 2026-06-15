package com.norton.backend.repositories;

import com.norton.backend.models.GeoProvinceModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeoProvinceRepository extends JpaRepository<GeoProvinceModel, Long> {
  Optional<GeoProvinceModel> findByCode(String code);
}
