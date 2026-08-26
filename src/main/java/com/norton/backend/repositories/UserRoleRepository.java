package com.norton.backend.repositories;

import com.norton.backend.models.UserRoleModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleModel, Long> {
  Optional<UserRoleModel> findByCode(String code);

  Optional<UserRoleModel> findByRoleName(String roleName);

  boolean existsByCode(String code);

  boolean existsByRoleName(String roleName);

  List<UserRoleModel> findAllByOrderByHierarchyLevelAsc();

  @Query(
      "SELECT r FROM UserRoleModel r WHERE "
          + "(:keyword IS NULL OR :keyword = '' OR LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%')) "
          + "OR LOWER(r.nameKm) LIKE LOWER(CONCAT('%', :keyword, '%')) "
          + "OR LOWER(r.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  Page<UserRoleModel> searchRoles(@Param("keyword") String keyword, Pageable pageable);
}
