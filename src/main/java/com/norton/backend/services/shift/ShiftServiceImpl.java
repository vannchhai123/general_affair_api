package com.norton.backend.services.shift;

import com.norton.backend.dto.request.ShiftUpsertRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.shifts.ShiftResponseDto;
import com.norton.backend.enums.ShiftAssignmentScope;
import com.norton.backend.enums.ShiftStatus;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.repositories.ShiftAssignmentRepository;
import com.norton.backend.repositories.ShiftRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

  private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");

  private final ShiftRepository shiftRepository;
  private final ShiftAssignmentRepository shiftAssignmentRepository;

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ShiftResponseDto> listShifts(
      String search, String status, int page, int size) {
    Specification<ShiftModel> spec = (root, query, cb) -> cb.conjunction();

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

    ShiftStatus parsedStatus = parseStatus(status, false);
    if (parsedStatus != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parsedStatus));
    }

    Page<ShiftModel> shiftPage = shiftRepository.findAll(spec, PageRequest.of(page, size));
    List<ShiftResponseDto> content = shiftPage.getContent().stream().map(this::toResponse).toList();

    return PageResponse.<ShiftResponseDto>builder()
        .content(content)
        .page(shiftPage.getNumber())
        .size(shiftPage.getSize())
        .totalElements(shiftPage.getTotalElements())
        .totalPages(shiftPage.getTotalPages())
        .last(shiftPage.isLast())
        .build();
  }

  @Override
  @Transactional
  public ShiftResponseDto createShift(ShiftUpsertRequest request) {
    String code = request.getCode().trim();
    if (shiftRepository.existsByCodeIgnoreCase(code)) {
      throw new ConflictException("Shift code already exists: " + code);
    }

    ShiftModel shift = new ShiftModel();
    applyUpsert(shift, request);
    ShiftModel saved = shiftRepository.save(shift);
    return toResponse(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public ShiftResponseDto getShiftById(Long id) {
    ShiftModel shift =
        shiftRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));
    return toResponse(shift);
  }

  @Override
  @Transactional
  public ShiftResponseDto updateShift(Long id, ShiftUpsertRequest request) {
    ShiftModel shift =
        shiftRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));

    String code = request.getCode().trim();
    if (shiftRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
      throw new ConflictException("Shift code already exists: " + code);
    }

    applyUpsert(shift, request);
    ShiftModel saved = shiftRepository.save(shift);
    return toResponse(saved);
  }

  @Override
  @Transactional
  public ShiftResponseDto updateShiftStatus(Long id, String status) {
    ShiftModel shift =
        shiftRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));
    ShiftStatus shiftStatus = parseStatus(status, true);
    shift.setStatus(shiftStatus);
    shift.setIsActive(shiftStatus == ShiftStatus.ACTIVE);
    return toResponse(shiftRepository.save(shift));
  }

  @Override
  @Transactional
  public void deleteShift(Long id) {
    ShiftModel shift =
        shiftRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));
    shiftRepository.delete(shift);
  }

  private void applyUpsert(ShiftModel shift, ShiftUpsertRequest request) {
    validateEffectiveDates(request.getEffectiveFrom(), request.getEffectiveTo());

    ShiftStatus status = parseStatus(request.getStatus(), true);
    boolean isActive =
        request.getIsActive() == null ? status == ShiftStatus.ACTIVE : request.getIsActive();
    if ((status == ShiftStatus.ACTIVE) != isActive) {
      throw new BadRequestException("status and isActive must be consistent");
    }

    shift.setName(request.getName().trim());
    shift.setCode(request.getCode().trim());
    shift.setStartTime(parseTime(request.getStartTime(), "startTime"));
    shift.setEndTime(parseTime(request.getEndTime(), "endTime"));
    shift.setStatus(status);
    shift.setIsActive(isActive);
    shift.setCrossMidnight(Boolean.TRUE.equals(request.getCrossMidnight()));
    shift.setGraceMinutes(nonNegativeOrZero(request.getGraceMinutes(), "graceMinutes"));
    shift.setCheckInOpenBeforeMinutes(
        nonNegativeOrZero(request.getCheckInOpenBeforeMinutes(), "checkInOpenBeforeMinutes"));
    shift.setCheckOutCloseAfterMinutes(
        nonNegativeOrZero(request.getCheckOutCloseAfterMinutes(), "checkOutCloseAfterMinutes"));
    shift.setEffectiveFrom(request.getEffectiveFrom());
    shift.setEffectiveTo(request.getEffectiveTo());
    shift.setDescription(trimToNull(request.getDescription()));
  }

  private ShiftResponseDto toResponse(ShiftModel shift) {
    long assignedDepartmentsCount =
        shiftAssignmentRepository.countDistinctScopeIdsByShiftIdAndScope(
            shift.getId(), ShiftAssignmentScope.DEPARTMENT);
    long assignedPositionsCount =
        shiftAssignmentRepository.countDistinctScopeIdsByShiftIdAndScope(
            shift.getId(), ShiftAssignmentScope.POSITION);
    long assignedEmployeesCount =
        shiftAssignmentRepository.countDistinctScopeIdsByShiftIdAndScope(
            shift.getId(), ShiftAssignmentScope.EMPLOYEE);

    return ShiftResponseDto.builder()
        .id(shift.getId())
        .name(shift.getName())
        .code(shift.getCode())
        .startTime(formatTime(shift.getStartTime()))
        .endTime(formatTime(shift.getEndTime()))
        .status(toLower(shift.getStatus()))
        .isActive(Boolean.TRUE.equals(shift.getIsActive()))
        .crossMidnight(Boolean.TRUE.equals(shift.getCrossMidnight()))
        .graceMinutes(orZero(shift.getGraceMinutes()))
        .checkInOpenBeforeMinutes(orZero(shift.getCheckInOpenBeforeMinutes()))
        .checkOutCloseAfterMinutes(orZero(shift.getCheckOutCloseAfterMinutes()))
        .effectiveFrom(shift.getEffectiveFrom())
        .effectiveTo(shift.getEffectiveTo())
        .description(shift.getDescription() == null ? "" : shift.getDescription())
        .assignedDepartmentsCount(assignedDepartmentsCount)
        .assignedPositionsCount(assignedPositionsCount)
        .assignedEmployeesCount(assignedEmployeesCount)
        .createdAt(shift.getCreatedAt())
        .updatedAt(shift.getUpdatedAt())
        .build();
  }

  private void validateEffectiveDates(LocalDate effectiveFrom, LocalDate effectiveTo) {
    if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
      throw new BadRequestException("effectiveTo must be >= effectiveFrom");
    }
  }

  private LocalTime parseTime(String value, String fieldName) {
    try {
      return value.length() == 5 ? LocalTime.parse(value, HH_MM) : LocalTime.parse(value, HH_MM_SS);
    } catch (Exception ex) {
      throw new BadRequestException(fieldName + " must be HH:mm or HH:mm:ss");
    }
  }

  private String formatTime(LocalTime value) {
    if (value == null) {
      return null;
    }
    if (value.getSecond() == 0 && value.getNano() == 0) {
      return value.format(HH_MM);
    }
    return value.format(HH_MM_SS);
  }

  private ShiftStatus parseStatus(String value, boolean required) {
    if (value == null || value.isBlank()) {
      if (required) {
        throw new BadRequestException("status is required");
      }
      return null;
    }
    try {
      return ShiftStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Invalid status. Expected: active, inactive");
    }
  }

  private String toLower(Enum<?> value) {
    return value == null ? null : value.name().toLowerCase(Locale.ROOT);
  }

  private Integer nonNegativeOrZero(Integer value, String fieldName) {
    if (value == null) {
      return 0;
    }
    if (value < 0) {
      throw new BadRequestException(fieldName + " must be >= 0");
    }
    return value;
  }

  private Integer orZero(Integer value) {
    return value == null ? 0 : value;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
