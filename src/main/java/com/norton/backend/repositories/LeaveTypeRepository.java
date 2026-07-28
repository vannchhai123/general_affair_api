package com.norton.backend.repositories;

import com.norton.backend.models.LeaveTypeModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveTypeModel, Long> {

  Optional<LeaveTypeModel> findByKey(String key);

  List<LeaveTypeModel> findByIsActiveTrueOrderByIdAsc();
}
