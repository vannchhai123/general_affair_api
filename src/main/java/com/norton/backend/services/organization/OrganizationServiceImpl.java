package com.norton.backend.services.organization;

import com.norton.backend.dto.request.DepartmentUpsertRequest;
import com.norton.backend.dto.request.PositionUpsertRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.organization.DepartmentResponseDto;
import com.norton.backend.dto.responses.organization.OrganizationSummaryResponse;
import com.norton.backend.dto.responses.organization.PositionResponseDto;
import com.norton.backend.enums.DepartmentStatus;
import com.norton.backend.enums.PositionStatus;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.DepartmentModel;
import com.norton.backend.models.PositionModel;
import com.norton.backend.repositories.DepartmentRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PositionRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final OfficerRepository officerRepository;

  @Override
  @Transactional(readOnly = true)
  public PageResponse<DepartmentResponseDto> listDepartments(
      String search, String status, Pageable pageable) {
    Specification<DepartmentModel> spec = (root, query, cb) -> cb.conjunction();

    if (search != null && !search.isBlank()) {
      String keyword = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
      spec =
          spec.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("name")), keyword),
                      cb.like(cb.lower(root.get("code")), keyword),
                      cb.like(cb.lower(root.get("manager")), keyword)));
    }

    DepartmentStatus parsedStatus = parseDepartmentStatus(status);
    if (parsedStatus != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parsedStatus));
    }

    Page<DepartmentModel> page = departmentRepository.findAll(spec, pageable);
    List<DepartmentResponseDto> content =
        page.getContent().stream().map(this::toDepartmentDto).toList();

    return PageResponse.<DepartmentResponseDto>builder()
        .content(content)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .build();
  }

  @Override
  @Transactional
  public DepartmentResponseDto createDepartment(DepartmentUpsertRequest request) {
    String normalizedCode = request.getCode().trim();
    if (departmentRepository.existsByCodeIgnoreCase(normalizedCode)) {
      throw new ConflictException("Department code already exists: " + normalizedCode);
    }

    DepartmentModel department =
        DepartmentModel.builder()
            .name(request.getName().trim())
            .code(normalizedCode)
            .manager(trimToNull(request.getManager()))
            .status(parseDepartmentStatusRequired(request.getStatus()))
            .description(trimToNull(request.getDescription()))
            .build();

    DepartmentModel saved = departmentRepository.save(department);
    return toDepartmentDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public DepartmentResponseDto getDepartmentById(Long id) {
    DepartmentModel department =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    return toDepartmentDto(department);
  }

  @Override
  @Transactional
  public DepartmentResponseDto updateDepartment(Long id, DepartmentUpsertRequest request) {
    DepartmentModel department =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

    String normalizedCode = request.getCode().trim();
    if (departmentRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
      throw new ConflictException("Department code already exists: " + normalizedCode);
    }

    department.setName(request.getName().trim());
    department.setCode(normalizedCode);
    department.setManager(trimToNull(request.getManager()));
    department.setStatus(parseDepartmentStatusRequired(request.getStatus()));
    department.setDescription(trimToNull(request.getDescription()));

    DepartmentModel updated = departmentRepository.save(department);
    return toDepartmentDto(updated);
  }

  @Override
  @Transactional
  public void deleteDepartment(Long id) {
    DepartmentModel department =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

    long positionCount = positionRepository.countByDepartment_Id(id);
    if (positionCount > 0) {
      throw new BadRequestException("Cannot delete department with existing positions");
    }

    departmentRepository.delete(department);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<PositionResponseDto> listPositions(
      String search, Long departmentId, String status, Pageable pageable) {
    Specification<PositionModel> spec = (root, query, cb) -> cb.conjunction();

    if (search != null && !search.isBlank()) {
      String keyword = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
      spec =
          spec.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("name")), keyword),
                      cb.like(cb.lower(root.get("code")), keyword),
                      cb.like(cb.lower(root.get("description")), keyword)));
    }

    if (departmentId != null) {
      spec =
          spec.and((root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId));
    }

    PositionStatus parsedStatus = parsePositionStatus(status);
    if (parsedStatus != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parsedStatus));
    }

    Page<PositionModel> page = positionRepository.findAll(spec, pageable);
    List<PositionResponseDto> content =
        page.getContent().stream().map(this::toPositionDto).toList();

    return PageResponse.<PositionResponseDto>builder()
        .content(content)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .build();
  }

  @Override
  @Transactional
  public PositionResponseDto createPosition(PositionUpsertRequest request) {
    String normalizedCode = request.getCode().trim();
    if (positionRepository.existsByCodeIgnoreCase(normalizedCode)) {
      throw new ConflictException("Position code already exists: " + normalizedCode);
    }

    DepartmentModel department =
        departmentRepository
            .findById(request.getDepartmentId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

    PositionModel position =
        PositionModel.builder()
            .name(request.getTitle().trim())
            .code(normalizedCode)
            .department(department)
            .status(parsePositionStatusRequired(request.getStatus()))
            .description(trimToNull(request.getDescription()))
            .build();

    PositionModel saved = positionRepository.save(position);
    return toPositionDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public PositionResponseDto getPositionById(Long id) {
    PositionModel position =
        positionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));
    return toPositionDto(position);
  }

  @Override
  @Transactional
  public PositionResponseDto updatePosition(Long id, PositionUpsertRequest request) {
    PositionModel position =
        positionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));

    String normalizedCode = request.getCode().trim();
    if (positionRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
      throw new ConflictException("Position code already exists: " + normalizedCode);
    }

    DepartmentModel department =
        departmentRepository
            .findById(request.getDepartmentId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

    position.setName(request.getTitle().trim());
    position.setCode(normalizedCode);
    position.setDepartment(department);
    position.setStatus(parsePositionStatusRequired(request.getStatus()));
    position.setDescription(trimToNull(request.getDescription()));

    PositionModel updated = positionRepository.save(position);
    return toPositionDto(updated);
  }

  @Override
  @Transactional
  public void deletePosition(Long id) {
    PositionModel position =
        positionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));

    long officerCount = officerRepository.countByPosition_Id(id);
    if (officerCount > 0) {
      throw new BadRequestException("Cannot delete position with assigned officers");
    }

    positionRepository.delete(position);
  }

  @Override
  @Transactional(readOnly = true)
  public OrganizationSummaryResponse getSummary() {
    return OrganizationSummaryResponse.builder()
        .departmentsTotal(departmentRepository.count())
        .departmentsActive(departmentRepository.countByStatus(DepartmentStatus.ACTIVE))
        .positionsTotal(positionRepository.count())
        .positionsActive(positionRepository.countByStatus(PositionStatus.ACTIVE))
        .assignedOfficers(officerRepository.countByPositionIsNotNull())
        .build();
  }

  private DepartmentResponseDto toDepartmentDto(DepartmentModel department) {
    return DepartmentResponseDto.builder()
        .id(department.getId())
        .uuid(department.getUuid())
        .name(department.getName())
        .code(department.getCode())
        .manager(department.getManager())
        .officerCount(officerRepository.countByPosition_Department_Id(department.getId()))
        .status(toLower(department.getStatus()))
        .description(department.getDescription())
        .build();
  }

  private PositionResponseDto toPositionDto(PositionModel position) {
    return PositionResponseDto.builder()
        .id(position.getId())
        .uuid(position.getUuid())
        .title(position.getName())
        .code(position.getCode())
        .departmentId(position.getDepartment() != null ? position.getDepartment().getId() : null)
        .departmentName(
            position.getDepartment() != null ? position.getDepartment().getName() : null)
        .officerCount(officerRepository.countByPosition_Id(position.getId()))
        .status(toLower(position.getStatus()))
        .description(position.getDescription())
        .build();
  }

  private DepartmentStatus parseDepartmentStatusRequired(String status) {
    DepartmentStatus parsed = parseDepartmentStatus(status);
    if (parsed == null) {
      throw new BadRequestException("Department status is required");
    }
    return parsed;
  }

  private PositionStatus parsePositionStatusRequired(String status) {
    PositionStatus parsed = parsePositionStatus(status);
    if (parsed == null) {
      throw new BadRequestException("Position status is required");
    }
    return parsed;
  }

  private DepartmentStatus parseDepartmentStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    try {
      return DepartmentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Invalid department status. Expected: active, inactive");
    }
  }

  private PositionStatus parsePositionStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    try {
      return PositionStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Invalid position status. Expected: active, inactive");
    }
  }

  private String toLower(Enum<?> value) {
    return value == null ? null : value.name().toLowerCase(Locale.ROOT);
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
