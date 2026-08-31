package com.norton.backend.controllers.superadmin;

import com.norton.backend.dto.responses.permissions.PermissionResponse;
import com.norton.backend.mapper.PermissionMapper;
import com.norton.backend.models.PermissionModel;
import com.norton.backend.repositories.PermissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SuperAdminPermissionController.BASE_URL)
public class SuperAdminPermissionController {

  public static final String BASE_URL = "/api/v1/super-admin/permissions";

  private final PermissionRepository permissionRepository;
  private final PermissionMapper permissionMapper;

  @GetMapping
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
    List<PermissionModel> permissions =
        permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    return ResponseEntity.ok(permissionMapper.toResponseList(permissions));
  }
}
