package com.norton.backend.services.role;

import com.norton.backend.dto.request.role.CreateRoleRequest;
import com.norton.backend.dto.request.role.UpdateRoleRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.role.RoleResponse;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.mapper.RoleMapper;
import com.norton.backend.models.PermissionModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.PermissionRepository;
import com.norton.backend.repositories.UserRoleRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleServiceImpl implements RoleService {

  private final UserRoleRepository userRoleRepository;
  private final PermissionRepository permissionRepository;
  private final RoleMapper roleMapper;

  @Override
  @Transactional(readOnly = true)
  public PageResponse<RoleResponse> getAllRoles(String keyword, Pageable pageable) {
    Page<UserRoleModel> page = userRoleRepository.searchRoles(keyword, pageable);
    List<RoleResponse> content = roleMapper.toResponseList(page.getContent());

    return PageResponse.<RoleResponse>builder()
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
  public List<RoleSimpleResponse> getSimpleRoles() {
    List<UserRoleModel> roles = userRoleRepository.findAllByOrderByHierarchyLevelAsc();
    return roleMapper.toSimpleResponseList(roles);
  }

  @Override
  @Transactional(readOnly = true)
  public RoleResponse getRoleById(Long id) {
    UserRoleModel role =
        userRoleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    return roleMapper.toResponse(role);
  }

  @Override
  public RoleResponse createRole(CreateRoleRequest request, UserModel currentUser) {
    String normalizedCode = request.getCode().trim().toUpperCase(Locale.ROOT);
    if (!normalizedCode.startsWith("ROLE_")) {
      normalizedCode = "ROLE_" + normalizedCode;
    }

    if (userRoleRepository.existsByCode(normalizedCode)
        || userRoleRepository.existsByRoleName(normalizedCode)) {
      throw new ConflictException("Role with code '" + normalizedCode + "' already exists");
    }

    assertCanAssignHierarchy(request.getHierarchyLevel(), currentUser);

    Set<PermissionModel> permissions = new HashSet<>();
    if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
      List<PermissionModel> found = permissionRepository.findAllByIdIn(request.getPermissionIds());
      if (found.size() != request.getPermissionIds().size()) {
        throw new BadRequestException("One or more permission IDs are invalid");
      }
      permissions.addAll(found);
    }

    UserRoleModel role =
        UserRoleModel.builder()
            .code(normalizedCode)
            .roleName(normalizedCode)
            .nameKm(request.getNameKm().trim())
            .nameEn(request.getNameEn() != null ? request.getNameEn().trim() : null)
            .hierarchyLevel(request.getHierarchyLevel())
            .description(request.getDescription() != null ? request.getDescription().trim() : null)
            .isSystem(false)
            .permissions(permissions)
            .build();

    UserRoleModel saved = userRoleRepository.save(role);
    log.info("Created new role: id={}, code={}", saved.getId(), saved.getCode());
    return roleMapper.toResponse(saved);
  }

  @Override
  public RoleResponse updateRole(Long id, UpdateRoleRequest request, UserModel currentUser) {
    UserRoleModel role =
        userRoleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

    assertCanManageRole(role, currentUser);
    assertCanAssignHierarchy(request.getHierarchyLevel(), currentUser);

    role.setNameKm(request.getNameKm().trim());
    if (request.getNameEn() != null) {
      role.setNameEn(request.getNameEn().trim());
    }
    role.setHierarchyLevel(request.getHierarchyLevel());
    if (request.getDescription() != null) {
      role.setDescription(request.getDescription().trim());
    }

    UserRoleModel updated = userRoleRepository.save(role);
    log.info("Updated role: id={}, code={}", updated.getId(), updated.getCode());
    return roleMapper.toResponse(updated);
  }

  @Override
  public void deleteRole(Long id, UserModel currentUser) {
    UserRoleModel role =
        userRoleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

    assertCanManageRole(role, currentUser);

    if (Boolean.TRUE.equals(role.getIsSystem())) {
      throw new BadRequestException("Cannot delete built-in system role: " + role.getCode());
    }

    if (role.getUsers() != null && !role.getUsers().isEmpty()) {
      throw new ConflictException(
          "Cannot delete role that is currently assigned to "
              + role.getUsers().size()
              + " user(s)");
    }

    role.getPermissions().clear();
    userRoleRepository.delete(role);
    log.info("Deleted role: id={}, code={}", id, role.getCode());
  }

  @Override
  public Map<String, Object> syncRolePermissions(
      Long roleId, List<Long> permissionIds, UserModel currentUser) {
    UserRoleModel role =
        userRoleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

    assertCanManageRole(role, currentUser);

    Set<PermissionModel> newPermissions = new HashSet<>();
    if (permissionIds != null && !permissionIds.isEmpty()) {
      List<PermissionModel> found = permissionRepository.findAllByIdIn(permissionIds);
      if (found.size() != permissionIds.size()) {
        throw new BadRequestException("One or more permission IDs are invalid");
      }
      newPermissions.addAll(found);
    }

    role.getPermissions().clear();
    role.getPermissions().addAll(newPermissions);
    userRoleRepository.save(role);

    List<String> permissionNames =
        role.getPermissions().stream().map(PermissionModel::getPermissionName).sorted().toList();

    log.info(
        "Synchronized permissions for role: id={}, totalPermissions={}",
        role.getId(),
        newPermissions.size());

    return Map.of(
        "success",
        true,
        "message",
        "Permissions synchronized successfully",
        "roleId",
        role.getId(),
        "syncedCount",
        role.getPermissions().size(),
        "permissions",
        permissionNames);
  }

  @Override
  public RoleResponse syncRolePermissionsAndReturn(
      Long roleId, List<Long> permissionIds, UserModel currentUser) {
    UserRoleModel role =
        userRoleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

    assertCanManageRole(role, currentUser);

    Set<PermissionModel> newPermissions = new HashSet<>();
    if (permissionIds != null && !permissionIds.isEmpty()) {
      List<PermissionModel> found = permissionRepository.findAllByIdIn(permissionIds);
      if (found.size() != permissionIds.size()) {
        throw new BadRequestException("One or more permission IDs are invalid");
      }
      newPermissions.addAll(found);
    }

    role.getPermissions().clear();
    role.getPermissions().addAll(newPermissions);
    UserRoleModel saved = userRoleRepository.save(role);

    log.info(
        "Synchronized permissions for role: id={}, totalPermissions={}",
        role.getId(),
        newPermissions.size());

    return roleMapper.toResponse(saved);
  }

  private void assertCanManageRole(UserRoleModel targetRole, UserModel currentUser) {
    if (currentUser == null || currentUser.getRole() == null) {
      throw new AccessDeniedException("Unauthorized to manage roles");
    }

    int callerLevel =
        currentUser.getRole().getHierarchyLevel() != null
            ? currentUser.getRole().getHierarchyLevel()
            : 99;

    int targetLevel = targetRole.getHierarchyLevel() != null ? targetRole.getHierarchyLevel() : 99;

    // Caller cannot manage roles that have equal or strictly higher authority (lower level number)
    // Exception: Super Admin (level 1) can manage everything
    if (callerLevel > 1 && targetLevel <= callerLevel) {
      throw new AccessDeniedException(
          "Privilege escalation denied: You cannot manage a role with hierarchy level ("
              + targetLevel
              + ") equal to or higher than your own ("
              + callerLevel
              + ")");
    }
  }

  private void assertCanAssignHierarchy(Integer targetHierarchyLevel, UserModel currentUser) {
    if (currentUser == null || currentUser.getRole() == null) {
      throw new AccessDeniedException("Unauthorized to assign role hierarchy");
    }

    int callerLevel =
        currentUser.getRole().getHierarchyLevel() != null
            ? currentUser.getRole().getHierarchyLevel()
            : 99;

    if (callerLevel > 1 && targetHierarchyLevel <= callerLevel) {
      throw new AccessDeniedException(
          "Privilege escalation denied: You cannot create or set a role hierarchy level ("
              + targetHierarchyLevel
              + ") equal to or higher than your own ("
              + callerLevel
              + ")");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<com.norton.backend.dto.responses.role.AdminRoleDto> getAdminRoles() {
    List<UserRoleModel> roles = userRoleRepository.findAllByOrderByHierarchyLevelAsc();
    return roles.stream()
        .map(
            r -> {
              List<String> perms =
                  r.getPermissions() != null
                      ? r.getPermissions().stream()
                          .map(PermissionModel::getPermissionName)
                          .filter(Objects::nonNull)
                          .sorted()
                          .toList()
                      : List.of();
              return com.norton.backend.dto.responses.role.AdminRoleDto.builder()
                  .id(r.getId())
                  .code(r.getRoleName())
                  .name(
                      r.getNameEn() != null
                          ? r.getNameEn()
                          : (r.getNameKm() != null ? r.getNameKm() : r.getRoleName()))
                  .nameKm(r.getNameKm())
                  .description(r.getDescription())
                  .permissions(perms)
                  .build();
            })
        .toList();
  }
}
