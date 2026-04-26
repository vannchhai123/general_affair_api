package com.norton.backend.repositories;

import com.norton.backend.enums.PositionStatus;
import com.norton.backend.models.PositionModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository
    extends JpaRepository<PositionModel, Long>, JpaSpecificationExecutor<PositionModel> {

  Optional<PositionModel> findByNameIgnoreCaseAndDepartment_NameIgnoreCase(
      String positionName, String departmentName);

  boolean existsByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

  long countByStatus(PositionStatus status);

  long countByDepartment_Id(Long departmentId);
}
