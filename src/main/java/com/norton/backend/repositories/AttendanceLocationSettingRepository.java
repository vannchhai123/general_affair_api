package com.norton.backend.repositories;

import com.norton.backend.models.AttendanceLocationSettingModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceLocationSettingRepository
    extends JpaRepository<AttendanceLocationSettingModel, Long> {

  Optional<AttendanceLocationSettingModel> findFirstByOrderByIdAsc();
}
