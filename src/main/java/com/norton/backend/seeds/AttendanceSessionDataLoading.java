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
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn({"attendanceDataLoading", "officerDataLoading"})
@RequiredArgsConstructor
@Order(8)
public class AttendanceSessionDataLoading implements CommandLineRunner {

  private final AttendanceSessionRepository attendanceSessionRepository;
  private final AttendanceRepository attendanceRepository;
  private final ShiftRepository shiftRepository;
  private final OfficerRepository officerRepository;

  @Override
  public void run(String... args) {
    List<ShiftModel> shifts = loadShifts();

    if (attendanceSessionRepository.count() > 0) {
      return;
    }

    AttendanceModel attendance1 =
        attendanceRepository
            .findByOfficerOfficerCodeAndDate("OFF-001", java.time.LocalDate.of(2026, 4, 14))
            .orElseThrow(() -> new RuntimeException("Attendance OFF-001 on 2026-04-14 not found"));

    AttendanceModel attendance2 =
        attendanceRepository
            .findByOfficerOfficerCodeAndDate("OFF-002", java.time.LocalDate.of(2026, 4, 14))
            .orElseThrow(() -> new RuntimeException("Attendance OFF-002 on 2026-04-14 not found"));

    OfficerModel officer1 =
        officerRepository
            .findByOfficerCode("OFF-001")
            .orElseThrow(() -> new RuntimeException("Officer OFF-001 not found"));

    OfficerModel officer2 =
        officerRepository
            .findByOfficerCode("OFF-002")
            .orElseThrow(() -> new RuntimeException("Officer OFF-002 not found"));

    ShiftModel morningShift = shifts.get(0);
    ShiftModel afternoonShift = shifts.get(1);

    List<AttendanceSessionModel> sessions =
        List.of(
            AttendanceSessionModel.builder()
                .uuid(UUID.randomUUID().toString())
                .attendance(attendance1)
                .shift(morningShift)
                .checkIn(LocalTime.of(8, 0))
                .checkOut(LocalTime.of(12, 30))
                .status("Present")
                .createdBy(officer1)
                .build(),
            AttendanceSessionModel.builder()
                .uuid(UUID.randomUUID().toString())
                .attendance(attendance1)
                .shift(afternoonShift)
                .checkIn(LocalTime.of(13, 0))
                .checkOut(LocalTime.of(18, 0))
                .status("Present")
                .createdBy(officer1)
                .build(),
            AttendanceSessionModel.builder()
                .uuid(UUID.randomUUID().toString())
                .attendance(attendance2)
                .shift(morningShift)
                .checkIn(LocalTime.of(8, 45))
                .checkOut(LocalTime.of(12, 30))
                .status("Late")
                .createdBy(officer2)
                .build(),
            AttendanceSessionModel.builder()
                .uuid(UUID.randomUUID().toString())
                .attendance(attendance2)
                .shift(afternoonShift)
                .checkIn(LocalTime.of(13, 0))
                .checkOut(LocalTime.of(18, 0))
                .status("Present")
                .createdBy(officer2)
                .build());

    attendanceSessionRepository.saveAll(sessions);

    System.out.println("Attendance session data loaded");
  }

  private List<ShiftModel> loadShifts() {
    ShiftModel morningShift =
        shiftRepository
            .findByName("Morning Shift")
            .orElseGet(() -> ShiftModel.builder().name("Morning Shift").build());
    morningShift.setStartTime(LocalTime.of(6, 0));
    morningShift.setEndTime(LocalTime.of(12, 30));
    morningShift.setIsActive(true);

    ShiftModel afternoonShift =
        shiftRepository
            .findByName("Afternoon Shift")
            .orElseGet(() -> ShiftModel.builder().name("Afternoon Shift").build());
    afternoonShift.setStartTime(LocalTime.of(13, 0));
    afternoonShift.setEndTime(LocalTime.of(18, 0));
    afternoonShift.setIsActive(true);

    return shiftRepository.saveAll(List.of(morningShift, afternoonShift));
  }
}
