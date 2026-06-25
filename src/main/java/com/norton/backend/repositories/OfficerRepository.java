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

  long countByStatusAndOffice_Id(OfficerStatus status, Long officeId);

  boolean existsByOfficerCode(String officerCode);

  boolean existsByEmail(String email);

  boolean existsByOfficerCodeAndIdNot(String officerCode, Long id);

  boolean existsByEmailAndIdNot(String email, Long id);

  Optional<OfficerModel> findByOfficerCode(String officerCode);

  java.util.List<OfficerModel> findByOffice_NameIgnoreCase(String officeName);

  @Query("SELECT COUNT(o) FROM OfficerModel o")
  long countAll();

  @Override
  @EntityGraph(
      attributePaths = {"user", "office", "position", "position.department", "educationLevel"})
  Page<OfficerModel> findAll(Pageable pageable);

  @EntityGraph(
      attributePaths = {"user", "office", "position", "position.department", "educationLevel"})
  Page<OfficerModel> findByPosition_Department_Id(Long departmentId, Pageable pageable);

  @EntityGraph(
      attributePaths = {"user", "office", "position", "position.department", "educationLevel"})
  Page<OfficerModel> findByOffice_Id(Long officeId, Pageable pageable);

  java.util.List<OfficerModel> findByOffice_Id(Long officeId);

  @Query(
      """
    SELECT o
    FROM OfficerModel o
    JOIN FETCH o.office
    JOIN FETCH o.position p
    JOIN FETCH p.department
    LEFT JOIN FETCH o.educationLevel
    WHERE o.id = :officerId
""")
  Optional<OfficerModel> findByIdWithPosition(Long officerId);

  @Query(
      """
    SELECT o
    FROM OfficerModel o
    JOIN FETCH o.office
    JOIN FETCH o.position p
    JOIN FETCH p.department
    LEFT JOIN FETCH o.educationLevel
    WHERE o.uuid = :officerUuid
""")
  Optional<OfficerModel> findByUuidWithPosition(String officerUuid);

  @Query(
      """
    SELECT o
    FROM OfficerModel o
    JOIN FETCH o.office
    JOIN FETCH o.position p
    JOIN FETCH p.department
    LEFT JOIN FETCH o.educationLevel
    WHERE o.user.id = :userId
""")
  Optional<OfficerModel> findByUserIdWithPosition(Long userId);

  long countByPosition_Department_Id(Long departmentId);

  long countByOffice_Id(Long officeId);

  long countByPosition_Id(Long positionId);

  long countByPositionIsNotNull();
}
