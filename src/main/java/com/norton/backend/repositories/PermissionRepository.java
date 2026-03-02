package com.norton.backend.repositories;

import com.norton.backend.models.PermissionModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionModel, Long> {

  List<PermissionModel> findAllByPermissionNameIn(List<String> permissionNames);

  Optional<PermissionModel> findByPermissionName(String permissionName);
}
