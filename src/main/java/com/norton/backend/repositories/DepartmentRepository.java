package com.norton.backend.repositories;

import com.norton.backend.models.DepartmentModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentModel, Long> {

  boolean existsByName(String name);

  Optional<DepartmentModel> findByNameIgnoreCase(String name);
}
