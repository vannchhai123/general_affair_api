package com.norton.backend.seeds;

import com.norton.backend.models.AttendanceModel;
import com.norton.backend.models.AttendanceStatusModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.AttendanceStatusRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn({"officerDataLoading", "attendanceStatusDataLoading"})
@RequiredArgsConstructor
@Order(7)
public class AttendanceDataLoading implements CommandLineRunner {

  private final AttendanceRepository attendanceRepository;
  private final OfficerRepository officerRepository;
  private final AttendanceStatusRepository statusRepository;

  @Override
  public void run(String... args) {

    if (attendanceRepository.count() > 0) return;

    OfficerModel officer1 =
        officerRepository
            .findByOfficerCode("OFF-001")
            .orElseThrow(() -> new RuntimeException("Officer OFF-001 not found"));

    OfficerModel officer2 =
        officerRepository
            .findByOfficerCode("OFF-002")
            .orElseThrow(() -> new RuntimeException("Officer OFF-002 not found"));

    AttendanceStatusModel present =
        statusRepository
            .findByCode("PRESENT")
            .orElseThrow(() -> new RuntimeException("Attendance status PRESENT not found"));

    AttendanceStatusModel late =
        statusRepository
            .findByCode("LATE")
            .orElseThrow(() -> new RuntimeException("Attendance status LATE not found"));

    AttendanceModel a1 =
        AttendanceModel.builder()
            .officer(officer1)
            .date(LocalDate.of(2026, 4, 14))
            .checkIn(LocalDateTime.of(2026, 4, 14, 8, 0))
            .checkOut(LocalDateTime.of(2026, 4, 14, 17, 0))
            .totalWorkMin(480)
            .totalLateMin(0)
            .status(present)
            .build();

    AttendanceModel a2 =
        AttendanceModel.builder()
            .officer(officer2)
            .date(LocalDate.of(2026, 4, 14))
            .checkIn(LocalDateTime.of(2026, 4, 14, 8, 45))
            .checkOut(LocalDateTime.of(2026, 4, 14, 17, 15))
            .totalWorkMin(450)
            .totalLateMin(45)
            .status(late)
            .build();

    attendanceRepository.saveAll(List.of(a1, a2));

    System.out.println("✅ Attendance data loaded");
  }
}
