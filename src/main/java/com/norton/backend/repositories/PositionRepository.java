package com.norton.backend.repositories;

import com.norton.backend.models.PositionModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<PositionModel, Long> {

  Optional<PositionModel> findByNameIgnoreCaseAndDepartment_NameIgnoreCase(
      String positionName, String departmentName);
}
