package com.norton.backend.repositories;

import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.models.OfficerModel;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficerRepository extends JpaRepository<OfficerModel, Long> {

  long countByStatus(OfficerStatus status);

  long countByStatusAndPosition_Department_Id(OfficerStatus status, Long departmentId);

  boolean existsByOfficerCode(String officerCode);

  boolean existsByEmail(String email);

  boolean existsByOfficerCodeAndIdNot(String officerCode, Long id);

  boolean existsByEmailAndIdNot(String email, Long id);

  Optional<OfficerModel> findByOfficerCode(String officerCode);

  @Query("SELECT COUNT(o) FROM OfficerModel o")
  long countAll();

  @Override
  @EntityGraph(attributePaths = {"user", "position", "position.department"})
  Page<OfficerModel> findAll(Pageable pageable);

  @EntityGraph(attributePaths = {"user", "position", "position.department"})
  Page<OfficerModel> findByPosition_Department_Id(Long departmentId, Pageable pageable);

  @Query(
      """
    SELECT o
    FROM OfficerModel o
    JOIN FETCH o.position p
    JOIN FETCH p.department
    WHERE o.id = :officerId
""")
  Optional<OfficerModel> findByIdWithPosition(Long officerId);

  @Query(
      """
    SELECT o
    FROM OfficerModel o
    JOIN FETCH o.position p
    JOIN FETCH p.department
    WHERE o.user.id = :userId
""")
  Optional<OfficerModel> findByUserIdWithPosition(Long userId);

  long countByPosition_Department_Id(Long departmentId);

  long countByPosition_Id(Long positionId);

  long countByPositionIsNotNull();
}
