package com.norton.backend.services.role;

import static java.time.LocalDateTime.*;

import com.norton.backend.dto.request.OfficerPermissionRequest;
import com.norton.backend.dto.request.PermissionRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.officers.OfficerPermissionResponse;
import com.norton.backend.dto.responses.permissions.PermissionResponse;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.mapper.OfficerPermissionMapper;
import com.norton.backend.mapper.PermissionMapper;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.OfficerPermission;
import com.norton.backend.models.PermissionModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.OfficerPermissionRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PermissionRepository;
import com.norton.backend.repositories.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

  private final PermissionRepository permissionRepository;
  private final UserRoleRepository userRoleRepository;
  private final PermissionMapper permissionMapper;
  private final OfficerPermissionRepository repository;
  private final OfficerRepository officerRepository;
  private final OfficerPermissionMapper mapper;

  public void assignPermissionsToRole(String roleName, List<String> permissionNames) {
    UserRoleModel role =
        userRoleRepository
            .findByRoleName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "roleName", roleName));

    System.out.println("Role: " + role);
    List<PermissionModel> permissions =
        permissionRepository.findAllByPermissionNameIn(permissionNames);

    System.out.println("Permission: " + permissions);
    if (permissions.size() != permissionNames.size()) {
      throw new RuntimeException("Some permissions not found");
    }
    role.getPermissions().addAll(permissions);
  }

  public PermissionResponse createPermission(PermissionRequest request) {
    if (permissionRepository.findByPermissionName(request.getPermissionName()).isPresent()) {
      throw new ConflictException("Permission already exists");
    }
    PermissionModel entity = permissionMapper.toEntity(request);
    PermissionModel saved = permissionRepository.save(entity);
    return permissionMapper.toResponse(saved);
  }

  public PageResponse<PermissionResponse> getPermissions(String category, Pageable pageable) {

    Page<PermissionModel> page;

    if (category != null) {
      page = permissionRepository.findByCategory(category, pageable);
    } else {
      page = permissionRepository.findAll(pageable);
    }

    List<PermissionResponse> permissions =
        page.getContent().stream().map(permissionMapper::toResponse).toList();

    return PageResponse.<PermissionResponse>builder()
        .content(permissions)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .build();
  }

  public PermissionResponse updatePermission(Long id, PermissionRequest request) {
    PermissionModel permission =
        permissionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    permission.setPermissionName(request.getPermissionName());
    permission.setDescription(request.getDescription());
    permission.setCategory(request.getCategory());

    PermissionModel updated = permissionRepository.save(permission);
    return permissionMapper.toResponse(updated);
  }

  public void deletePermission(Long id) {
    PermissionModel permission =
        permissionRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found"));

    for (UserRoleModel role : permission.getRoles()) {
      role.getPermissions().remove(permission);
    }
    permissionRepository.delete(permission);
  }

  public PermissionResponse getPermission(Long id) {
    PermissionModel permission =
        permissionRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
    return permissionMapper.toResponse(permission);
  }

  public PageResponse<PermissionResponse> getPermissions(
      String category, String keyword, Pageable pageable) {

    Page<PermissionModel> page;

    if (keyword != null && !keyword.isBlank()) {
      page = permissionRepository.findByPermissionNameContainingIgnoreCase(keyword, pageable);
    } else if (category != null && !category.isBlank()) {
      page = permissionRepository.findByCategory(category, pageable);
    } else {
      page = permissionRepository.findAll(pageable);
    }

    List<PermissionResponse> permissions =
        page.getContent().stream().map(permissionMapper::toResponse).toList();

    return PageResponse.<PermissionResponse>builder()
        .content(permissions)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .build();
  }

  public OfficerPermissionResponse create(OfficerPermissionRequest request, Long grantedBy) {

    OfficerModel officer =
        officerRepository
            .findById(request.getOfficerId())
            .orElseThrow(() -> new EntityNotFoundException("Officer not found"));

    PermissionModel permission =
        permissionRepository
            .findById(request.getPermissionId())
            .orElseThrow(() -> new EntityNotFoundException("Permission not found"));

    OfficerPermission entity =
        OfficerPermission.builder()
            .officer(officer)
            .permission(permission)
            .grantedAt(now())
            .grantedBy(grantedBy)
            .build();

    repository.save(entity);

    return mapper.toResponse(entity);
  }
}
