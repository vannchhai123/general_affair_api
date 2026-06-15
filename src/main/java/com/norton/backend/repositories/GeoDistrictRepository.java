package com.norton.backend.repositories;

import com.norton.backend.models.GeoDistrictModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeoDistrictRepository extends JpaRepository<GeoDistrictModel, Long> {
  Optional<GeoDistrictModel> findByCode(String code);
}
