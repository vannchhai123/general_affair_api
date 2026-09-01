package com.norton.backend.services.superadmin;

import com.norton.backend.dto.request.superadmin.AdminResetPasswordRequest;
import com.norton.backend.dto.request.superadmin.AssignUserRoleRequest;
import com.norton.backend.dto.request.superadmin.SyncUserAccessRequest;
import com.norton.backend.dto.request.superadmin.UpdateUserStatusRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.dto.responses.superadmin.SuperAdminUserDetailResponse;
import com.norton.backend.dto.responses.superadmin.UserAccessDetailResponse;
import com.norton.backend.dto.responses.superadmin.UserAccessResponse;
import com.norton.backend.dto.responses.superadmin.UserAccessResponse.RoleAccessDto;
import com.norton.backend.dto.responses.superadmin.UserAccessResponse.UserAccessData;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.mapper.RoleMapper;
import com.norton.backend.models.AuditLogModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.OfficerPermission;
import com.norton.backend.models.PermissionModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.AuditLogRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PermissionRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
  private final OfficerRepository officerRepository;
  private final PermissionRepository permissionRepository;
  private final AuditLogRepository auditLogRepository;
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
        .first(page.isFirst())
        .last(page.isLast())
        .empty(page.isEmpty())
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

    java.util.Set<Long> roleIdsToAssign = new java.util.HashSet<>();
    if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
      roleIdsToAssign.addAll(request.getRoleIds());
    }
    if (request.getRoleId() != null) {
      roleIdsToAssign.add(request.getRoleId());
    }

    if (roleIdsToAssign.isEmpty()) {
      throw new BadRequestException("At least one valid Role ID must be provided");
    }

    List<UserRoleModel> rolesFound = userRoleRepository.findAllById(roleIdsToAssign);
    java.util.Set<Long> foundIds =
        rolesFound.stream().map(UserRoleModel::getId).collect(java.util.stream.Collectors.toSet());
    java.util.Set<Long> missingIds =
        roleIdsToAssign.stream()
            .filter(rId -> !foundIds.contains(rId))
            .collect(java.util.stream.Collectors.toSet());

    if (!missingIds.isEmpty()) {
      throw new BadRequestException("Role ID(s) not found in database: " + missingIds);
    }

    for (UserRoleModel newRole : rolesFound) {
      assertCanAssignRole(newRole, currentUser);
    }

    user.getRoles().clear();
    user.getRoles().addAll(rolesFound);
    UserModel saved = userRepository.save(user);
    log.info("Assigned {} role(s) to user id={}", rolesFound.size(), id);
    return toDetailResponse(saved);
  }

  @Override
  public void resetUserPassword(Long id, AdminResetPasswordRequest request, UserModel currentUser) {
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

    int targetLevel = targetRole.getHierarchyLevel() != null ? targetRole.getHierarchyLevel() : 99;

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
    RoleSimpleResponse primaryRoleResponse =
        user.getRole() != null ? roleMapper.toSimpleResponse(user.getRole()) : null;

    List<RoleSimpleResponse> allRolesResponse =
        user.getRoles() != null
            ? user.getRoles().stream().map(roleMapper::toSimpleResponse).toList()
            : List.of();

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
        .uuid(user.getUuid() != null ? user.getUuid().toString() : null)
        .username(user.getUsername())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .userStatus(user.getUserStatus())
        .imageUrl(imageUrl)
        .role(primaryRoleResponse)
        .roles(allRolesResponse)
        .officerId(officerId)
        .officerCode(officerCode)
        .departmentName(departmentName)
        .positionName(positionName)
        .permissions(permissions)
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  @Override
  public UserAccessResponse syncUserAccess(
      Long userId, SyncUserAccessRequest request, UserModel currentUser) {
    UserModel user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    OfficerModel officer =
        user.getOfficer() != null
            ? user.getOfficer()
            : officerRepository.findByUserId(userId).orElse(null);

    return syncUserAccessInternal(user, officer, request, currentUser);
  }

  @Override
  public UserAccessResponse syncOfficerAccess(
      Long officerId, SyncUserAccessRequest request, UserModel currentUser) {
    OfficerModel officer =
        officerRepository
            .findById(officerId)
            .orElseGet(
                () ->
                    officerRepository
                        .findByUserId(officerId)
                        .orElseThrow(
                            () -> new ResourceNotFoundException("Officer", "id", officerId)));

    UserModel user = officer.getUser();
    if (user == null) {
      throw new BadRequestException("Officer has no linked user account to manage access");
    }

    return syncUserAccessInternal(user, officer, request, currentUser);
  }

  private UserAccessResponse syncUserAccessInternal(
      UserModel user, OfficerModel officer, SyncUserAccessRequest request, UserModel currentUser) {
    if (request == null) {
      throw new BadRequestException("Request body cannot be null");
    }

    // 1. Sync Roles
    if (request.getRoleIds() != null) {
      if (!request.getRoleIds().isEmpty()) {
        List<UserRoleModel> rolesFound = userRoleRepository.findAllById(request.getRoleIds());
        Set<Long> foundIds =
            rolesFound.stream().map(UserRoleModel::getId).collect(Collectors.toSet());
        Set<Long> missingIds =
            request.getRoleIds().stream()
                .filter(rId -> !foundIds.contains(rId))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
          throw new BadRequestException("Role ID(s) not found in database: " + missingIds);
        }

        for (UserRoleModel newRole : rolesFound) {
          assertCanAssignRole(newRole, currentUser);
        }

        if (user.getRoles() == null) {
          user.setRoles(new HashSet<>(rolesFound));
        } else {
          try {
            user.getRoles().clear();
            user.getRoles().addAll(rolesFound);
          } catch (UnsupportedOperationException e) {
            user.setRoles(new HashSet<>(rolesFound));
          }
        }
      } else {
        if (user.getRoles() != null) {
          try {
            user.getRoles().clear();
          } catch (UnsupportedOperationException e) {
            user.setRoles(new HashSet<>());
          }
        }
      }
      user = userRepository.save(user);
    }

    // 2. Sync Direct Permissions on Officer
    if (request.getDirectPermissions() != null) {
      if (officer == null) {
        if (!request.getDirectPermissions().isEmpty()) {
          throw new BadRequestException(
              "No associated officer profile found for user id "
                  + user.getId()
                  + " to attach direct permissions");
        }
      } else {
        if (officer.getOfficerPermissions() == null) {
          officer.setOfficerPermissions(new ArrayList<>());
        } else {
          try {
            officer.getOfficerPermissions().clear();
          } catch (UnsupportedOperationException e) {
            officer.setOfficerPermissions(new ArrayList<>());
          }
        }

        if (!request.getDirectPermissions().isEmpty()) {
          List<PermissionModel> permsFound =
              permissionRepository.findAllByPermissionNameIn(request.getDirectPermissions());
          Set<String> foundPermNames =
              permsFound.stream()
                  .map(PermissionModel::getPermissionName)
                  .collect(Collectors.toSet());
          List<String> missingPerms =
              request.getDirectPermissions().stream()
                  .filter(p -> !foundPermNames.contains(p))
                  .toList();

          if (!missingPerms.isEmpty()) {
            throw new BadRequestException("Permission(s) not found in database: " + missingPerms);
          }

          for (PermissionModel perm : permsFound) {
            OfficerPermission op =
                OfficerPermission.builder()
                    .officer(officer)
                    .permission(perm)
                    .grantedAt(LocalDateTime.now())
                    .grantedBy(currentUser != null ? currentUser.getId() : null)
                    .build();
            officer.getOfficerPermissions().add(op);
          }
        }
        officer = officerRepository.save(officer);
      }
    }

    // 3. Audit Logging
    if (auditLogRepository != null) {
      try {
        AuditLogModel auditLog =
            AuditLogModel.builder()
                .actorId(currentUser != null ? currentUser.getId() : null)
                .actorName(currentUser != null ? currentUser.getFullName() : null)
                .actorEmail(currentUser != null ? currentUser.getEmail() : null)
                .action("SYNC_USER_ACCESS")
                .entityType("USER")
                .entityId(user.getId())
                .details(
                    request.getReason() != null
                        ? request.getReason()
                        : "Synced roles and direct permissions")
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(auditLog);
      } catch (Exception ex) {
        log.warn("Failed to write audit log for access sync: {}", ex.getMessage());
      }
    }

    // 4. Compute Effective Permissions
    Set<String> effectivePerms = new LinkedHashSet<>();
    if (user.getRoles() != null) {
      user.getRoles().stream()
          .sorted(
              Comparator.comparing(r -> r.getHierarchyLevel() != null ? r.getHierarchyLevel() : 99))
          .forEach(
              r -> {
                if (r.getPermissions() != null) {
                  r.getPermissions()
                      .forEach(
                          p -> {
                            if (p.getPermissionName() != null) {
                              effectivePerms.add(p.getPermissionName());
                            }
                          });
                }
              });
    }

    if (officer != null && officer.getOfficerPermissions() != null) {
      officer
          .getOfficerPermissions()
          .forEach(
              op -> {
                if (op.getPermission() != null && op.getPermission().getPermissionName() != null) {
                  effectivePerms.add(op.getPermission().getPermissionName());
                }
              });
    }

    // 5. Build Assigned Roles DTOs
    List<RoleAccessDto> assignedRoles =
        user.getRoles() != null
            ? user.getRoles().stream()
                .sorted(
                    Comparator.comparing(
                        r -> r.getHierarchyLevel() != null ? r.getHierarchyLevel() : 99))
                .map(
                    r ->
                        RoleAccessDto.builder()
                            .id(r.getId())
                            .code(r.getRoleName())
                            .name(
                                r.getNameEn() != null
                                    ? r.getNameEn()
                                    : (r.getNameKm() != null ? r.getNameKm() : r.getRoleName()))
                            .nameKm(r.getNameKm())
                            .build())
                .toList()
            : List.of();

    // 6. Build Direct Permissions DTOs
    List<String> directPermsList =
        officer != null && officer.getOfficerPermissions() != null
            ? officer.getOfficerPermissions().stream()
                .map(
                    op ->
                        op.getPermission() != null ? op.getPermission().getPermissionName() : null)
                .filter(Objects::nonNull)
                .toList()
            : List.of();

    UserAccessData data =
        UserAccessData.builder()
            .officerId(officer != null ? officer.getId() : null)
            .userId(user.getId())
            .assignedRoles(assignedRoles)
            .directPermissions(directPermsList)
            .effectivePermissions(new ArrayList<>(effectivePerms))
            .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt() : Instant.now())
            .build();

    log.info(
        "Successfully synced access for user id={}, officer id={}. Effective permissions count={}",
        user.getId(),
        officer != null ? officer.getId() : null,
        effectivePerms.size());

    return UserAccessResponse.builder()
        .success(true)
        .message("Officer roles and permissions updated successfully")
        .data(data)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public UserAccessDetailResponse getUserAccessDetails(Long userId) {
    UserModel user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    OfficerModel officer =
        user.getOfficer() != null
            ? user.getOfficer()
            : officerRepository.findByUserId(userId).orElse(null);
    return toUserAccessDetailResponse(user, officer);
  }

  @Override
  @Transactional(readOnly = true)
  public UserAccessDetailResponse getOfficerAccessDetails(Long officerId) {
    OfficerModel officer =
        officerRepository
            .findById(officerId)
            .orElseGet(
                () ->
                    officerRepository
                        .findByUserId(officerId)
                        .orElseThrow(
                            () -> new ResourceNotFoundException("Officer", "id", officerId)));
    UserModel user = officer.getUser();
    if (user == null) {
      throw new BadRequestException("Officer has no linked user account");
    }
    return toUserAccessDetailResponse(user, officer);
  }

  private UserAccessDetailResponse toUserAccessDetailResponse(
      UserModel user, OfficerModel officer) {
    Set<String> effectivePerms = new LinkedHashSet<>();

    List<UserAccessDetailResponse.AssignedRoleDetailDto> assignedRoles =
        user.getRoles() != null
            ? user.getRoles().stream()
                .sorted(
                    Comparator.comparing(
                        r -> r.getHierarchyLevel() != null ? r.getHierarchyLevel() : 99))
                .map(
                    r -> {
                      List<String> rolePerms =
                          r.getPermissions() != null
                              ? r.getPermissions().stream()
                                  .map(PermissionModel::getPermissionName)
                                  .filter(Objects::nonNull)
                                  .toList()
                              : List.of();
                      effectivePerms.addAll(rolePerms);
                      return UserAccessDetailResponse.AssignedRoleDetailDto.builder()
                          .id(r.getId())
                          .code(r.getRoleName())
                          .name(
                              r.getNameEn() != null
                                  ? r.getNameEn()
                                  : (r.getNameKm() != null ? r.getNameKm() : r.getRoleName()))
                          .nameKm(r.getNameKm())
                          .permissions(rolePerms)
                          .build();
                    })
                .toList()
            : List.of();

    List<String> directPerms =
        officer != null && officer.getOfficerPermissions() != null
            ? officer.getOfficerPermissions().stream()
                .map(
                    op ->
                        op.getPermission() != null ? op.getPermission().getPermissionName() : null)
                .filter(Objects::nonNull)
                .toList()
            : List.of();

    effectivePerms.addAll(directPerms);

    return UserAccessDetailResponse.builder()
        .officerId(officer != null ? officer.getId() : null)
        .userId(user.getId())
        .fullName(user.getFullName())
        .assignedRoles(assignedRoles)
        .directPermissions(directPerms)
        .effectivePermissions(new ArrayList<>(effectivePerms))
        .build();
  }
}
