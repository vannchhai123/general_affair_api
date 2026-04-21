package com.norton.backend.repositories;

import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.models.OfficerModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficerRepository extends JpaRepository<OfficerModel, Long> {

  long countByStatus(OfficerStatus status);

  Optional<OfficerModel> findByOfficerCode(String officerCode);

  @Query("SELECT COUNT(o) FROM OfficerModel o")
  long countAll();

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
}
