package com.norton.backend.repositories;

import com.norton.backend.models.LeaveRequestModel;
import java.time.LocalDate;
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

  long countByStatusIgnoreCase(String status);

  @Query(
      "SELECT COUNT(lr) > 0 FROM LeaveRequestModel lr "
          + "WHERE lr.officer.id = :officerId "
          + "AND LOWER(lr.status) NOT IN ('rejected', 'cancelled') "
          + "AND lr.startDate <= :endDate "
          + "AND lr.endDate >= :startDate")
  boolean existsOverlappingRequest(
      @Param("officerId") Long officerId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query(
      "SELECT COUNT(lr) > 0 FROM LeaveRequestModel lr "
          + "WHERE lr.officer.id = :officerId "
          + "AND lr.id <> :excludeId "
          + "AND LOWER(lr.status) NOT IN ('rejected', 'cancelled') "
          + "AND lr.startDate <= :endDate "
          + "AND lr.endDate >= :startDate")
  boolean existsOverlappingRequestExcludingId(
      @Param("officerId") Long officerId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("excludeId") Long excludeId);

  @Query(
      "SELECT lr FROM LeaveRequestModel lr "
          + "WHERE lr.officer.id = :officerId "
          + "AND LOWER(lr.status) = 'approved' "
          + "AND lr.startDate <= :endDate "
          + "AND lr.endDate >= :startDate")
  List<LeaveRequestModel> findApprovedOverlapping(
      @Param("officerId") Long officerId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
