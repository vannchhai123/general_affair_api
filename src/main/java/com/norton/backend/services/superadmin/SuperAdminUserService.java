package com.norton.backend.services.superadmin;

import com.norton.backend.dto.request.superadmin.AdminResetPasswordRequest;
import com.norton.backend.dto.request.superadmin.AssignUserRoleRequest;
import com.norton.backend.dto.request.superadmin.UpdateUserStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.superadmin.SuperAdminUserDetailResponse;
import com.norton.backend.models.UserModel;
import org.springframework.data.domain.Pageable;

public interface SuperAdminUserService {

  PageResponse<SuperAdminUserDetailResponse> getAllUsers(String keyword, Pageable pageable);

  SuperAdminUserDetailResponse getUserById(Long id);

  SuperAdminUserDetailResponse updateUserStatus(
      Long id, UpdateUserStatusRequest request, UserModel currentUser);

  SuperAdminUserDetailResponse assignUserRole(
      Long id, AssignUserRoleRequest request, UserModel currentUser);

  void resetUserPassword(Long id, AdminResetPasswordRequest request, UserModel currentUser);
}
