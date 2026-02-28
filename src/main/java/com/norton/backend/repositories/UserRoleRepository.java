package com.norton.backend.repositories;

import com.norton.backend.models.UserRoleModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleModel, Long> {
  Optional<UserRoleModel> findByRoleName(String roleName);

  boolean existsByRoleName(String roleName);
}
