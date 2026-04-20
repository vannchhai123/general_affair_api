package com.norton.backend.repositories;

import com.norton.backend.models.AttendanceStatusModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceStatusRepository extends JpaRepository<AttendanceStatusModel, Long> {
  Optional<AttendanceStatusModel> findByCode(String code);

  Optional<AttendanceStatusModel> findByNameIgnoreCase(String name);
}
