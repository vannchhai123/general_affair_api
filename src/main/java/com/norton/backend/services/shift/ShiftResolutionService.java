package com.norton.backend.services.shift;

import com.norton.backend.enums.ShiftAssignmentScope;
import com.norton.backend.enums.ShiftDayOfWeek;
import com.norton.backend.enums.ShiftStatus;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.ShiftAssignmentModel;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.repositories.ShiftAssignmentRepository;
import com.norton.backend.repositories.ShiftRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShiftResolutionService {

  private final ShiftAssignmentRepository shiftAssignmentRepository;
  private final ShiftRepository shiftRepository;

  @Transactional(readOnly = true)
  public Optional<ShiftWindow> resolveOfficerShift(OfficerModel officer, LocalDateTime dateTime) {
    List<ShiftWindow> assignedWindows = findAssignedShiftWindows(officer, dateTime);
    if (!assignedWindows.isEmpty()) {
      return assignedWindows.stream().min(Comparator.comparing(ShiftWindow::startsAt));
    }

    return resolveActiveShift(dateTime);
  }

  @Transactional(readOnly = true)
  public Optional<ShiftWindow> resolveActiveShift(LocalDateTime dateTime) {
    return shiftRepository.findAll().stream()
        .filter(Objects::nonNull)
        .map(shift -> toWindow(shift, dateTime))
        .flatMap(Optional::stream)
        .filter(window -> isWithinScanWindow(dateTime, window))
        .min(Comparator.comparing(ShiftWindow::startsAt));
  }

  public boolean isCheckInAllowed(LocalDateTime dateTime, ShiftWindow window) {
    return !dateTime.isBefore(window.checkInOpensAt()) && !dateTime.isAfter(window.endsAt());
  }

  public int calculateLateMinutes(LocalDateTime checkInDateTime, ShiftModel shift) {
    LocalDateTime lateAfter =
        LocalDateTime.of(checkInDateTime.toLocalDate(), shift.getStartTime())
            .plusMinutes(orZero(shift.getGraceMinutes()));
    if (!checkInDateTime.isAfter(lateAfter)) {
      return 0;
    }
    return (int) java.time.Duration.between(lateAfter, checkInDateTime).toMinutes();
  }

  public String shiftType(ShiftModel shift) {
    return "shift_" + shift.getId();
  }

  public String shiftLabel(ShiftModel shift) {
    if (shift.getName() != null && !shift.getName().isBlank()) {
      return shift.getName();
    }
    if (shift.getCode() != null && !shift.getCode().isBlank()) {
      return shift.getCode();
    }
    return shiftType(shift);
  }

  @Transactional(readOnly = true)
  public Optional<ShiftModel> findShiftByType(String shiftType) {
    if (shiftType == null || !shiftType.startsWith("shift_")) {
      return Optional.empty();
    }
    try {
      return shiftRepository.findById(Long.parseLong(shiftType.substring("shift_".length())));
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }
  }

  private List<ShiftWindow> findAssignedShiftWindows(OfficerModel officer, LocalDateTime dateTime) {
    if (officer == null || officer.getId() == null || officer.getPosition() == null) {
      return List.of();
    }

    Long positionId = officer.getPosition().getId();
    Long departmentId =
        officer.getPosition().getDepartment() != null
            ? officer.getPosition().getDepartment().getId()
            : null;
    List<Long> scopeIds = new ArrayList<>();
    scopeIds.add(officer.getId());
    if (positionId != null) {
      scopeIds.add(positionId);
    }
    if (departmentId != null) {
      scopeIds.add(departmentId);
    }

    if (scopeIds.isEmpty()) {
      return List.of();
    }

    return possibleShiftDates(dateTime).stream()
        .flatMap(
            shiftDate ->
                shiftAssignmentRepository
                    .findEffectiveAssignments(
                        List.of(
                            ShiftAssignmentScope.EMPLOYEE,
                            ShiftAssignmentScope.POSITION,
                            ShiftAssignmentScope.DEPARTMENT),
                        scopeIds,
                        toShiftDayOfWeek(shiftDate.getDayOfWeek()),
                        shiftDate)
                    .stream())
        .filter(assignment -> matchesOfficerScope(assignment, officer, positionId, departmentId))
        .sorted(Comparator.comparingInt(this::scopePriority))
        .map(ShiftAssignmentModel::getShift)
        .filter(Objects::nonNull)
        .map(shift -> toWindow(shift, dateTime))
        .flatMap(Optional::stream)
        .filter(window -> isWithinScanWindow(dateTime, window))
        .toList();
  }

  private Optional<ShiftWindow> toWindow(ShiftModel shift, LocalDateTime dateTime) {
    if (!isActiveShift(shift)) {
      return Optional.empty();
    }

    return possibleShiftDates(dateTime).stream()
        .filter(date -> isEffectiveOn(shift, date))
        .map(date -> buildWindow(shift, date))
        .filter(window -> isWithinScanWindow(dateTime, window))
        .findFirst();
  }

  private ShiftWindow buildWindow(ShiftModel shift, LocalDate date) {
    LocalDateTime startsAt = LocalDateTime.of(date, shift.getStartTime());
    LocalDate endDate = isCrossMidnight(shift) ? date.plusDays(1) : date;
    LocalDateTime endsAt = LocalDateTime.of(endDate, shift.getEndTime());
    return new ShiftWindow(
        shift,
        date,
        startsAt,
        endsAt,
        startsAt.minusMinutes(orZero(shift.getCheckInOpenBeforeMinutes())),
        endsAt.plusMinutes(orZero(shift.getCheckOutCloseAfterMinutes())));
  }

  private List<LocalDate> possibleShiftDates(LocalDateTime dateTime) {
    return List.of(dateTime.toLocalDate(), dateTime.toLocalDate().minusDays(1));
  }

  private boolean isWithinScanWindow(LocalDateTime dateTime, ShiftWindow window) {
    return !dateTime.isBefore(window.checkInOpensAt())
        && !dateTime.isAfter(window.checkOutClosesAt());
  }

  private boolean isActiveShift(ShiftModel shift) {
    return shift.getStartTime() != null
        && shift.getEndTime() != null
        && shift.getStatus() == ShiftStatus.ACTIVE
        && Boolean.TRUE.equals(shift.getIsActive());
  }

  private boolean isEffectiveOn(ShiftModel shift, LocalDate date) {
    return (shift.getEffectiveFrom() == null || !shift.getEffectiveFrom().isAfter(date))
        && (shift.getEffectiveTo() == null || !shift.getEffectiveTo().isBefore(date));
  }

  private boolean isCrossMidnight(ShiftModel shift) {
    LocalTime startTime = shift.getStartTime();
    LocalTime endTime = shift.getEndTime();
    return Boolean.TRUE.equals(shift.getCrossMidnight())
        || (startTime != null && endTime != null && !endTime.isAfter(startTime));
  }

  private boolean matchesOfficerScope(
      ShiftAssignmentModel assignment, OfficerModel officer, Long positionId, Long departmentId) {
    return switch (assignment.getScope()) {
      case EMPLOYEE -> assignment.getScopeId().equals(officer.getId());
      case POSITION -> assignment.getScopeId().equals(positionId);
      case DEPARTMENT -> assignment.getScopeId().equals(departmentId);
    };
  }

  private int scopePriority(ShiftAssignmentModel assignment) {
    return switch (assignment.getScope()) {
      case EMPLOYEE -> 0;
      case POSITION -> 1;
      case DEPARTMENT -> 2;
    };
  }

  private ShiftDayOfWeek toShiftDayOfWeek(DayOfWeek dayOfWeek) {
    return switch (dayOfWeek) {
      case MONDAY -> ShiftDayOfWeek.MON;
      case TUESDAY -> ShiftDayOfWeek.TUE;
      case WEDNESDAY -> ShiftDayOfWeek.WED;
      case THURSDAY -> ShiftDayOfWeek.THU;
      case FRIDAY -> ShiftDayOfWeek.FRI;
      case SATURDAY -> ShiftDayOfWeek.SAT;
      case SUNDAY -> ShiftDayOfWeek.SUN;
    };
  }

  private int orZero(Integer value) {
    return value == null ? 0 : value;
  }

  public record ShiftWindow(
      ShiftModel shift,
      LocalDate shiftDate,
      LocalDateTime startsAt,
      LocalDateTime endsAt,
      LocalDateTime checkInOpensAt,
      LocalDateTime checkOutClosesAt) {}
}
