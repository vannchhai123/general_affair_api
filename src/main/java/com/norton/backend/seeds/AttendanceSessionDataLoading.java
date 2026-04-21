package com.norton.backend.seeds;

import com.norton.backend.models.AttendanceModel;
import com.norton.backend.models.AttendanceSessionModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.ShiftModel;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.AttendanceSessionRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.ShiftRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Order(8)
public class AttendanceSessionDataLoading implements CommandLineRunner {

  private final AttendanceSessionRepository attendanceSessionRepository;
  private final AttendanceRepository attendanceRepository;
  private final ShiftRepository shiftRepository;
  private final OfficerRepository officerRepository;

  @Override
  public void run(String... args) {
    if (attendanceSessionRepository.count() > 0) {
      return;
    }

    AttendanceModel attendance1 =
        attendanceRepository
            .findById(1L)
            .orElseThrow(() -> new RuntimeException("Attendance 1 not found"));

    AttendanceModel attendance2 =
        attendanceRepository
            .findById(2L)
            .orElseThrow(() -> new RuntimeException("Attendance 2 not found"));

    OfficerModel officer1 =
        officerRepository
            .findById(1L)
            .orElseThrow(() -> new RuntimeException("Officer 1 not found"));

    OfficerModel officer2 =
        officerRepository
            .findById(2L)
            .orElseThrow(() -> new RuntimeException("Officer 2 not found"));

    List<ShiftModel> shifts = loadShifts();
    ShiftModel morningShift = shifts.get(0);
    ShiftModel afternoonShift = shifts.get(1);

    List<AttendanceSessionModel> sessions =
        List.of(
            AttendanceSessionModel.builder()
                .uuid(UUID.randomUUID().toString())
                .attendance(attendance1)
                .shift(morningShift)
                .checkIn(LocalTime.of(8, 0))
                .checkOut(LocalTime.of(12, 0))
                .status("Present")
                .createdBy(officer1)
                .build(),
            AttendanceSessionModel.builder()
                .uuid(UUID.randomUUID().toString())
                .attendance(attendance1)
                .shift(afternoonShift)
                .checkIn(LocalTime.of(13, 0))
                .checkOut(LocalTime.of(17, 0))
                .status("Present")
                .createdBy(officer1)
                .build(),
            AttendanceSessionModel.builder()
                .uuid(UUID.randomUUID().toString())
                .attendance(attendance2)
                .shift(morningShift)
                .checkIn(LocalTime.of(8, 45))
                .checkOut(LocalTime.of(12, 0))
                .status("Late")
                .createdBy(officer2)
                .build(),
            AttendanceSessionModel.builder()
                .uuid(UUID.randomUUID().toString())
                .attendance(attendance2)
                .shift(afternoonShift)
                .checkIn(LocalTime.of(13, 0))
                .checkOut(LocalTime.of(17, 15))
                .status("Present")
                .createdBy(officer2)
                .build());

    attendanceSessionRepository.saveAll(sessions);

    System.out.println("Attendance session data loaded");
  }

  private List<ShiftModel> loadShifts() {
    if (shiftRepository.count() > 0) {
      List<ShiftModel> existingShifts = shiftRepository.findAll();
      if (existingShifts.size() >= 2) {
        return existingShifts;
      }
    }

    ShiftModel morningShift =
        ShiftModel.builder()
            .name("Morning Shift")
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(12, 0))
            .isActive(true)
            .build();

    ShiftModel afternoonShift =
        ShiftModel.builder()
            .name("Afternoon Shift")
            .startTime(LocalTime.of(13, 0))
            .endTime(LocalTime.of(17, 0))
            .isActive(true)
            .build();

    return shiftRepository.saveAll(List.of(morningShift, afternoonShift));
  }
}
