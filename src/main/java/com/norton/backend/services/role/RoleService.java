package com.norton.backend.services.role;

import com.norton.backend.dto.request.role.CreateRoleRequest;
import com.norton.backend.dto.request.role.UpdateRoleRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.role.RoleResponse;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.models.UserModel;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public interface RoleService {

  PageResponse<RoleResponse> getAllRoles(String keyword, Pageable pageable);

  List<RoleSimpleResponse> getSimpleRoles();

  RoleResponse getRoleById(Long id);

  RoleResponse createRole(CreateRoleRequest request, UserModel currentUser);

  RoleResponse updateRole(Long id, UpdateRoleRequest request, UserModel currentUser);

  void deleteRole(Long id, UserModel currentUser);

  Map<String, Object> syncRolePermissions(
      Long roleId, List<Long> permissionIds, UserModel currentUser);

  RoleResponse syncRolePermissionsAndReturn(
      Long roleId, List<Long> permissionIds, UserModel currentUser);
}
