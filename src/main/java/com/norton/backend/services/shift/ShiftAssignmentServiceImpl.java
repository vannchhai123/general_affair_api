package com.norton.backend.services.shift;

import com.norton.backend.dto.request.ShiftAssignmentTemplateRequest;
import com.norton.backend.dto.responses.shifts.ShiftAssignmentResponseDto;
import com.norton.backend.dto.responses.shifts.ShiftAssignmentTemplateResponseDto;
import com.norton.backend.enums.ShiftAssignmentScope;
import com.norton.backend.enums.ShiftDayOfWeek;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.ShiftAssignmentModel;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.repositories.ShiftAssignmentRepository;
import com.norton.backend.repositories.ShiftRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService {

  private static final List<String> DAY_KEYS =
      List.of("mon", "tue", "wed", "thu", "fri", "sat", "sun");

  private final ShiftAssignmentRepository shiftAssignmentRepository;
  private final ShiftRepository shiftRepository;

  @Override
  @Transactional(readOnly = true)
  public List<ShiftAssignmentResponseDto> listAssignments(String scope, Long id) {
    List<ShiftAssignmentModel> assignments;
    if ((scope == null || scope.isBlank()) && id == null) {
      assignments = shiftAssignmentRepository.findAllByOrderByScopeAscScopeIdAscDayOfWeekAscIdAsc();
    } else {
      if (scope == null || scope.isBlank() || id == null) {
        throw new BadRequestException("scope and id must be provided together");
      }
      ShiftAssignmentScope parsedScope = parseScope(scope);
      assignments =
          shiftAssignmentRepository.findByScopeAndScopeIdOrderByDayOfWeekAscIdAsc(parsedScope, id);
    }

    return assignments.stream().map(this::toAssignmentResponse).toList();
  }

  @Override
  @Transactional
  public ShiftAssignmentTemplateResponseDto saveTemplate(ShiftAssignmentTemplateRequest request) {
    validateEffectiveDates(request.getEffectiveFrom(), request.getEffectiveTo());
    ShiftAssignmentScope scope = parseScope(request.getScope());
    Map<ShiftDayOfWeek, List<Long>> normalizedDays = normalizeDays(request.getDays());
    Set<Long> shiftIds =
        normalizedDays.values().stream().flatMap(List::stream).collect(Collectors.toSet());
    shiftIds.add(request.getId());

    Map<Long, ShiftModel> shiftsById =
        shiftRepository.findAllById(shiftIds).stream()
            .collect(Collectors.toMap(ShiftModel::getId, Function.identity()));
    for (Long shiftId : shiftIds) {
      if (!shiftsById.containsKey(shiftId)) {
        throw new ResourceNotFoundException("Shift", "id", shiftId);
      }
    }

    shiftAssignmentRepository.deleteTemplateRows(
        scope, request.getScopeId(), request.getEffectiveFrom(), request.getEffectiveTo());

    List<ShiftAssignmentModel> rows = new ArrayList<>();
    for (Map.Entry<ShiftDayOfWeek, List<Long>> dayEntry : normalizedDays.entrySet()) {
      for (Long shiftId : dayEntry.getValue()) {
        rows.add(
            ShiftAssignmentModel.builder()
                .shift(shiftsById.get(shiftId))
                .scope(scope)
                .scopeId(request.getScopeId())
                .scopeName(trimToNull(request.getScopeName()))
                .dayOfWeek(dayEntry.getKey())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build());
      }
    }
    shiftAssignmentRepository.saveAll(rows);

    return ShiftAssignmentTemplateResponseDto.builder()
        .id(request.getId())
        .shiftId(request.getId())
        .scope(scope.name().toLowerCase(Locale.ROOT))
        .scopeId(request.getScopeId())
        .scopeName(request.getScopeName())
        .effectiveFrom(request.getEffectiveFrom())
        .effectiveTo(request.getEffectiveTo())
        .days(
            normalizedDays.entrySet().stream()
                .collect(
                    Collectors.toMap(
                        e -> e.getKey().name().toLowerCase(Locale.ROOT),
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new)))
        .build();
  }

  private ShiftAssignmentResponseDto toAssignmentResponse(ShiftAssignmentModel assignment) {
    return ShiftAssignmentResponseDto.builder()
        .id(assignment.getId())
        .shiftId(assignment.getShift() != null ? assignment.getShift().getId() : null)
        .scope(toLower(assignment.getScope()))
        .scopeId(assignment.getScopeId())
        .scopeName(assignment.getScopeName())
        .dayOfWeek(toLower(assignment.getDayOfWeek()))
        .effectiveFrom(assignment.getEffectiveFrom())
        .effectiveTo(assignment.getEffectiveTo())
        .build();
  }

  private ShiftAssignmentScope parseScope(String value) {
    try {
      return ShiftAssignmentScope.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (Exception ex) {
      throw new BadRequestException("Invalid scope. Expected: department, position, employee");
    }
  }

  private Map<ShiftDayOfWeek, List<Long>> normalizeDays(Map<String, List<Long>> days) {
    Map<ShiftDayOfWeek, List<Long>> normalized = new LinkedHashMap<>();
    for (String key : DAY_KEYS) {
      normalized.put(parseDayOfWeek(key), List.of());
    }

    for (Map.Entry<String, List<Long>> entry : days.entrySet()) {
      ShiftDayOfWeek day = parseDayOfWeek(entry.getKey());
      List<Long> values = entry.getValue() == null ? List.of() : entry.getValue();
      if (values.stream().anyMatch(v -> v == null || v <= 0)) {
        throw new BadRequestException("days values must be positive shift ids");
      }
      normalized.put(day, new ArrayList<>(new LinkedHashSet<>(values)));
    }

    return normalized;
  }

  private ShiftDayOfWeek parseDayOfWeek(String value) {
    try {
      return ShiftDayOfWeek.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (Exception ex) {
      throw new BadRequestException("Invalid dayOfWeek. Expected: " + String.join(", ", DAY_KEYS));
    }
  }

  private void validateEffectiveDates(LocalDate effectiveFrom, LocalDate effectiveTo) {
    if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
      throw new BadRequestException("effectiveTo must be >= effectiveFrom");
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
