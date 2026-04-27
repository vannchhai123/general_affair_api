package com.norton.backend.seeds;

import com.norton.backend.models.AttendanceModel;
import com.norton.backend.models.AttendanceSessionModel;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.AttendanceSessionRepository;
import com.norton.backend.repositories.ShiftRepository;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn({"attendanceDataLoading", "officerDataLoading"})
@RequiredArgsConstructor
@Order(8)
public class AttendanceSessionDataLoading implements CommandLineRunner {

  private static final LocalTime MORNING_START = LocalTime.of(8, 0);
  private static final LocalTime MORNING_END = LocalTime.of(12, 30);
  private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
  private static final LocalTime AFTERNOON_END = LocalTime.of(17, 0);

  private final AttendanceSessionRepository attendanceSessionRepository;
  private final AttendanceRepository attendanceRepository;
  private final ShiftRepository shiftRepository;

  @Override
  public void run(String... args) {
    List<ShiftModel> shifts = loadShifts();
    ShiftModel morningShift = shifts.get(0);
    ShiftModel afternoonShift = shifts.get(1);

    Set<String> existingKeys = new HashSet<>();
    for (AttendanceSessionModel session : attendanceSessionRepository.findAll()) {
      if (session.getAttendance() != null
          && session.getAttendance().getId() != null
          && session.getShift() != null
          && session.getShift().getId() != null) {
        existingKeys.add(buildKey(session.getAttendance().getId(), session.getShift().getId()));
      }
    }

    List<AttendanceSessionModel> sessionsToInsert = new ArrayList<>();
    for (AttendanceModel attendance : attendanceRepository.findAll()) {
      Long attendanceId = attendance.getId();
      if (attendanceId == null) {
        continue;
      }

      String morningKey = buildKey(attendanceId, morningShift.getId());
      if (!existingKeys.contains(morningKey)) {
        sessionsToInsert.add(buildMorningSession(attendance, morningShift));
        existingKeys.add(morningKey);
      }

      String afternoonKey = buildKey(attendanceId, afternoonShift.getId());
      if (!existingKeys.contains(afternoonKey)) {
        sessionsToInsert.add(buildAfternoonSession(attendance, afternoonShift));
        existingKeys.add(afternoonKey);
      }
    }

    if (!sessionsToInsert.isEmpty()) {
      attendanceSessionRepository.saveAll(sessionsToInsert);
    }

    System.out.println("Attendance sessions seed data inserted/updated successfully.");
  }

  private AttendanceSessionModel buildMorningSession(
      AttendanceModel attendance, ShiftModel morningShift) {
    LocalTime checkIn =
        attendance.getCheckIn() != null ? attendance.getCheckIn().toLocalTime() : null;
    LocalTime checkOut =
        attendance.getCheckOut() != null
            ? minTime(attendance.getCheckOut().toLocalTime(), MORNING_END)
            : null;

    if (checkIn != null && checkIn.isAfter(MORNING_END)) {
      checkIn = MORNING_START;
    }
    if (checkOut != null && checkOut.isBefore(checkIn != null ? checkIn : MORNING_START)) {
      checkOut = MORNING_END;
    }

    String status = resolveSessionStatus(attendance, checkIn, checkOut, true);
    return AttendanceSessionModel.builder()
        .uuid(UUID.randomUUID().toString())
        .attendance(attendance)
        .shift(morningShift)
        .checkIn(checkIn)
        .checkOut(checkOut)
        .status(status)
        .createdBy(attendance.getOfficer())
        .build();
  }

  private AttendanceSessionModel buildAfternoonSession(
      AttendanceModel attendance, ShiftModel afternoonShift) {
    LocalTime checkIn = attendance.getCheckIn() != null ? AFTERNOON_START : null;
    LocalTime checkOut =
        attendance.getCheckOut() != null ? attendance.getCheckOut().toLocalTime() : null;

    if (checkOut != null && checkOut.isBefore(AFTERNOON_START)) {
      checkOut = AFTERNOON_END;
    }

    String status = resolveSessionStatus(attendance, checkIn, checkOut, false);
    return AttendanceSessionModel.builder()
        .uuid(UUID.randomUUID().toString())
        .attendance(attendance)
        .shift(afternoonShift)
        .checkIn(checkIn)
        .checkOut(checkOut)
        .status(status)
        .createdBy(attendance.getOfficer())
        .build();
  }

  private String resolveSessionStatus(
      AttendanceModel attendance, LocalTime checkIn, LocalTime checkOut, boolean morning) {
    if (attendance.getCheckIn() == null || attendance.getCheckOut() == null) {
      return "Absent";
    }

    if (morning && checkIn != null && checkIn.isAfter(LocalTime.of(8, 0))) {
      return "Late";
    }

    if (checkIn != null && checkOut != null) {
      return "Present";
    }
    return "Pending";
  }

  private LocalTime minTime(LocalTime left, LocalTime right) {
    return left.isBefore(right) ? left : right;
  }

  private List<ShiftModel> loadShifts() {
    ShiftModel morningShift =
        shiftRepository
            .findByName("Morning Shift")
            .orElseGet(() -> ShiftModel.builder().name("Morning Shift").code("MORNING").build());
    morningShift.setStartTime(LocalTime.of(6, 0));
    morningShift.setEndTime(LocalTime.of(12, 30));
    morningShift.setIsActive(true);

    ShiftModel afternoonShift =
        shiftRepository
            .findByName("Afternoon Shift")
            .orElseGet(
                () -> ShiftModel.builder().name("Afternoon Shift").code("AFTERNOON").build());
    afternoonShift.setStartTime(LocalTime.of(13, 0));
    afternoonShift.setEndTime(LocalTime.of(18, 0));
    afternoonShift.setIsActive(true);

    return shiftRepository.saveAll(List.of(morningShift, afternoonShift));
  }

  private String buildKey(Long attendanceId, Long shiftId) {
    return attendanceId + "|" + shiftId;
  }
}
