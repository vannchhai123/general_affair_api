package com.norton.backend.repositories;

import com.norton.backend.models.PermissionModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionModel, Long> {

  List<PermissionModel> findAllByPermissionNameIn(List<String> permissionNames);

  Optional<PermissionModel> findByPermissionName(String permissionName);

  Page<PermissionModel> findAll(Pageable pageable);

  Page<PermissionModel> findByCategory(String category, Pageable pageable);

  Page<PermissionModel> findByPermissionNameContainingIgnoreCase(String keyword, Pageable pageable);
}
