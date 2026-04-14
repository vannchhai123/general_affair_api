package com.norton.backend.services.role;

import com.norton.backend.dto.responses.OfficerPermissionResponse;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.mapper.OfficerPermissionMapper;
import com.norton.backend.models.OfficerPermission;
import com.norton.backend.repositories.OfficerPermissionRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class OfficerPermissionService {

  private final OfficerPermissionRepository officerPermissionRepository;
  private final OfficerPermissionMapper officerPermissionMapper;

  public PageResponse<OfficerPermissionResponse> getAllPermissions(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    Page<OfficerPermission> result = officerPermissionRepository.findAll(pageable);

    List<OfficerPermissionResponse> content =
        result.getContent().stream().map(officerPermissionMapper::toDto).toList();

    return PageResponse.<OfficerPermissionResponse>builder()
        .content(content)
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .last(result.isLast())
        .build();
  }
}
