package com.norton.backend.services.superadmin;

import com.norton.backend.dto.request.superadmin.AdminResetPasswordRequest;
import com.norton.backend.dto.request.superadmin.AssignUserRoleRequest;
import com.norton.backend.dto.request.superadmin.UpdateUserStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.dto.responses.superadmin.SuperAdminUserDetailResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.mapper.RoleMapper;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SuperAdminUserServiceImpl implements SuperAdminUserService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final RoleMapper roleMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public PageResponse<SuperAdminUserDetailResponse> getAllUsers(String keyword, Pageable pageable) {
    Page<UserModel> page = userRepository.searchUsers(keyword, pageable);
    List<SuperAdminUserDetailResponse> content =
        page.getContent().stream().map(this::toDetailResponse).toList();

    return PageResponse.<SuperAdminUserDetailResponse>builder()
        .content(content)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public SuperAdminUserDetailResponse getUserById(Long id) {
    UserModel user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return toDetailResponse(user);
  }

  @Override
  public SuperAdminUserDetailResponse updateUserStatus(
      Long id, UpdateUserStatusRequest request, UserModel currentUser) {
    UserModel user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    if (user.getId().equals(currentUser.getId())) {
      throw new BadRequestException("You cannot change your own account status");
    }

    user.setUserStatus(request.getStatus());
    UserModel saved = userRepository.save(user);
    log.info("Updated status for user id={}, newStatus={}", id, request.getStatus());
    return toDetailResponse(saved);
  }

  @Override
  public SuperAdminUserDetailResponse assignUserRole(
      Long id, AssignUserRoleRequest request, UserModel currentUser) {
    UserModel user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    UserRoleModel newRole =
        userRoleRepository
            .findById(request.getRoleId())
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

    assertCanAssignRole(newRole, currentUser);

    user.setRole(newRole);
    UserModel saved = userRepository.save(user);
    log.info("Assigned role '{}' to user id={}", newRole.getCode(), id);
    return toDetailResponse(saved);
  }

  @Override
  public void resetUserPassword(
      Long id, AdminResetPasswordRequest request, UserModel currentUser) {
    UserModel user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
    userRepository.save(user);
    log.info("Super Admin reset password for user id={}", id);
  }

  private void assertCanAssignRole(UserRoleModel targetRole, UserModel currentUser) {
    if (currentUser == null || currentUser.getRole() == null) {
      throw new AccessDeniedException("Unauthorized to assign role");
    }

    int callerLevel =
        currentUser.getRole().getHierarchyLevel() != null
            ? currentUser.getRole().getHierarchyLevel()
            : 99;

    int targetLevel =
        targetRole.getHierarchyLevel() != null ? targetRole.getHierarchyLevel() : 99;

    if (callerLevel > 1 && targetLevel < callerLevel) {
      throw new AccessDeniedException(
          "Privilege escalation denied: You cannot assign a role with higher hierarchy ("
              + targetLevel
              + ") than your own ("
              + callerLevel
              + ")");
    }
  }

  private SuperAdminUserDetailResponse toDetailResponse(UserModel user) {
    RoleSimpleResponse roleResponse =
        user.getRole() != null ? roleMapper.toSimpleResponse(user.getRole()) : null;

    List<String> permissions =
        user.getAuthorities() != null
            ? user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
            : List.of();

    String officerCode = null;
    Long officerId = null;
    String departmentName = null;
    String positionName = null;
    String imageUrl = user.getImageUrl();

    if (user.getOfficer() != null) {
      officerId = user.getOfficer().getId();
      officerCode = user.getOfficer().getOfficerCode();
      if (user.getOfficer().getImageUrl() != null) {
        imageUrl = user.getOfficer().getImageUrl();
      }
      if (user.getOfficer().getOffice() != null) {
        departmentName =
            user.getOfficer().getOffice().getNameKh() != null
                ? user.getOfficer().getOffice().getNameKh()
                : user.getOfficer().getOffice().getName();
      }
      if (user.getOfficer().getPosition() != null) {
        positionName = user.getOfficer().getPosition().getName();
      }
    }

    return SuperAdminUserDetailResponse.builder()
        .id(user.getId())
        .uuid(user.getUuid())
        .username(user.getUsername())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .userStatus(user.getUserStatus())
        .imageUrl(imageUrl)
        .role(roleResponse)
        .officerId(officerId)
        .officerCode(officerCode)
        .departmentName(departmentName)
        .positionName(positionName)
        .permissions(permissions)
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
