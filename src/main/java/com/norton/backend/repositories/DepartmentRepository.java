package com.norton.backend.repositories;

import com.norton.backend.enums.DepartmentStatus;
import com.norton.backend.models.DepartmentModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository
    extends JpaRepository<DepartmentModel, Long>, JpaSpecificationExecutor<DepartmentModel> {

  boolean existsByName(String name);

  Optional<DepartmentModel> findByNameIgnoreCase(String name);

  Optional<DepartmentModel> findByAdmin_Id(Long adminId);

  boolean existsByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

  boolean existsByAdmin_Id(Long adminId);

  boolean existsByAdmin_IdAndIdNot(Long adminId, Long id);

  long countByStatus(DepartmentStatus status);
}
