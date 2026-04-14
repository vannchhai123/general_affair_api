package com.norton.backend.repositories;

import com.norton.backend.models.OfficerPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficerPermissionRepository extends JpaRepository<OfficerPermission, Long> {

  @Query(
      """
    SELECT op FROM OfficerPermission op
    JOIN FETCH op.officer o
    JOIN FETCH o.user u
    JOIN FETCH op.permission p
""")
  Page<OfficerPermission> findAllWithRelations(Pageable pageable);
}
