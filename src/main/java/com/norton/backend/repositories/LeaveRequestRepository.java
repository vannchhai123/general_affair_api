package com.norton.backend.repositories;

import com.norton.backend.models.LeaveRequestModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestModel, Long> {
  List<LeaveRequestModel> findAllByOrderByIdDesc();

  List<LeaveRequestModel> findByOfficerIdOrderByIdDesc(Long officerId);

  long countByStatus(String status);
}
