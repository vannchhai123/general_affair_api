package com.norton.backend.repositories;

import com.norton.backend.models.LeaveRequestModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestModel, Long> {
  List<LeaveRequestModel> findAllByOrderByIdDesc();

  List<LeaveRequestModel> findByOfficerIdOrderByIdDesc(Long officerId);

  @Query(
      "SELECT lr FROM LeaveRequestModel lr WHERE lr.officer.id = :id OR (lr.officer.user IS NOT NULL AND lr.officer.user.id = :id) ORDER BY lr.id DESC")
  List<LeaveRequestModel> findByOfficerIdOrUserIdOrderByIdDesc(@Param("id") Long id);

  long countByStatus(String status);
}
