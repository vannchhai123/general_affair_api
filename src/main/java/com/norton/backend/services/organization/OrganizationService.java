package com.norton.backend.services.organization;

import com.norton.backend.dto.request.DepartmentUpsertRequest;
import com.norton.backend.dto.request.PositionUpsertRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.organization.DepartmentResponseDto;
import com.norton.backend.dto.responses.organization.OrganizationSummaryResponse;
import com.norton.backend.dto.responses.organization.PositionResponseDto;
import org.springframework.data.domain.Pageable;

public interface OrganizationService {
  PageResponse<DepartmentResponseDto> listDepartments(
      String search, String status, Pageable pageable);

  DepartmentResponseDto createDepartment(DepartmentUpsertRequest request);

  DepartmentResponseDto getDepartmentById(Long id);

  DepartmentResponseDto updateDepartment(Long id, DepartmentUpsertRequest request);

  void deleteDepartment(Long id);

  PageResponse<PositionResponseDto> listPositions(
      String search, Long departmentId, String status, Pageable pageable);

  PositionResponseDto createPosition(PositionUpsertRequest request);

  PositionResponseDto getPositionById(Long id);

  PositionResponseDto updatePosition(Long id, PositionUpsertRequest request);

  void deletePosition(Long id);

  OrganizationSummaryResponse getSummary();
}
