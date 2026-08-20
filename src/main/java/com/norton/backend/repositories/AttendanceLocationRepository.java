package com.norton.backend.repositories;

import com.norton.backend.models.AttendanceLocationModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceLocationRepository extends JpaRepository<AttendanceLocationModel, Long> {

  List<AttendanceLocationModel> findAllByOrderByIdAsc();
}
