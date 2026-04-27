package com.norton.backend.seeds;

import com.norton.backend.models.AttendanceModel;
import com.norton.backend.models.AttendanceStatusModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.AttendanceRepository;
import com.norton.backend.repositories.AttendanceStatusRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    List<OfficerModel> officers = loadSeedOfficers();
    if (officers.isEmpty()) {
      return;
    }

    AttendanceStatusModel present = getRequiredStatus("PRESENT");
    AttendanceStatusModel late = getRequiredStatus("LATE");
    AttendanceStatusModel approved = getRequiredStatus("APPROVED");
    AttendanceStatusModel absent = getRequiredStatus("ABSENT");

    YearMonth month = YearMonth.now(ZoneId.of("Asia/Phnom_Penh"));
    List<AttendanceModel> records = new ArrayList<>();
    Set<String> plannedKeys = new HashSet<>();

    for (int officerIndex = 0; officerIndex < officers.size(); officerIndex++) {
      OfficerModel officer = officers.get(officerIndex);
      for (int day = 1; day <= month.lengthOfMonth(); day++) {
        LocalDate date = month.atDay(day);
        String key = buildKey(officer.getId(), date);
        if (isWeekend(date)
            || plannedKeys.contains(key)
            || attendanceRepository.existsByOfficerIdAndDate(officer.getId(), date)) {
          continue;
        }
        records.add(
            buildDailyAttendance(officer, officerIndex, date, present, late, approved, absent));
        plannedKeys.add(key);
      }
    }

    // Keep this date guaranteed for attendance-session seed compatibility.
    addIfMissing(
        records,
        plannedKeys,
        "OFF-001",
        LocalDate.of(2026, 4, 14),
        present,
        late,
        approved,
        absent);
    addIfMissing(
        records,
        plannedKeys,
        "OFF-002",
        LocalDate.of(2026, 4, 14),
        present,
        late,
        approved,
        absent);
    addIfMissing(
        records,
        plannedKeys,
        "OFF-005",
        LocalDate.of(2026, 4, 14),
        present,
        late,
        approved,
        absent);

    if (!records.isEmpty()) {
      attendanceRepository.saveAll(records);
    }

    System.out.println("Monthly attendance seed data inserted/updated successfully.");
  }

  private List<OfficerModel> loadSeedOfficers() {
    return officerRepository.findAll().stream()
        .filter(
            officer ->
                officer.getOfficerCode() != null && officer.getOfficerCode().startsWith("OFF-"))
        .sorted(Comparator.comparing(OfficerModel::getOfficerCode))
        .limit(20)
        .toList();
  }

  private AttendanceStatusModel getRequiredStatus(String code) {
    return statusRepository
        .findByCode(code)
        .orElseThrow(() -> new RuntimeException("Attendance status " + code + " not found"));
  }

  private AttendanceModel buildDailyAttendance(
      OfficerModel officer,
      int officerIndex,
      LocalDate date,
      AttendanceStatusModel present,
      AttendanceStatusModel late,
      AttendanceStatusModel approved,
      AttendanceStatusModel absent) {
    int score = ((officerIndex + 1) * 31) + (date.getDayOfMonth() * 17);

    boolean isAbsent = score % 11 == 0;
    boolean isLate = !isAbsent && score % 4 == 0;
    boolean isApproved = !isAbsent && score % 3 == 0;

    if (isAbsent) {
      return AttendanceModel.builder()
          .officer(officer)
          .date(date)
          .checkIn(null)
          .checkOut(null)
          .totalWorkMin(0)
          .totalLateMin(0)
          .status(absent)
          .notes("អវត្តមាន")
          .build();
    }

    int lateMinutes = isLate ? 5 + (score % 35) : 0;
    LocalDateTime checkIn = date.atTime(8, 0).plusMinutes(lateMinutes);
    LocalDateTime checkOut = date.atTime(17, 0).plusMinutes(score % 16);
    int totalWorkMin = (int) java.time.Duration.between(checkIn, checkOut).toMinutes();

    AttendanceStatusModel status = isApproved ? approved : (isLate ? late : present);
    return AttendanceModel.builder()
        .officer(officer)
        .date(date)
        .checkIn(checkIn)
        .checkOut(checkOut)
        .totalWorkMin(Math.max(totalWorkMin, 0))
        .totalLateMin(lateMinutes)
        .status(status)
        .notes("ទិន្នន័យសាកល្បងប្រចាំខែ")
        .build();
  }

  private void addIfMissing(
      List<AttendanceModel> records,
      Set<String> plannedKeys,
      String officerCode,
      LocalDate date,
      AttendanceStatusModel present,
      AttendanceStatusModel late,
      AttendanceStatusModel approved,
      AttendanceStatusModel absent) {
    OfficerModel officer =
        officerRepository
            .findByOfficerCode(officerCode)
            .orElseThrow(() -> new RuntimeException("Officer " + officerCode + " not found"));

    String key = buildKey(officer.getId(), date);
    if (plannedKeys.contains(key)
        || attendanceRepository.existsByOfficerIdAndDate(officer.getId(), date)) {
      return;
    }

    int officerIndex = parseOfficerIndex(officerCode);
    records.add(buildDailyAttendance(officer, officerIndex, date, present, late, approved, absent));
    plannedKeys.add(key);
  }

  private int parseOfficerIndex(String officerCode) {
    try {
      return Math.max(Integer.parseInt(officerCode.replace("OFF-", "")) - 1, 0);
    } catch (Exception ex) {
      return 0;
    }
  }

  private boolean isWeekend(LocalDate date) {
    DayOfWeek day = date.getDayOfWeek();
    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
  }

  private String buildKey(Long officerId, LocalDate date) {
    return officerId + "|" + date;
  }
}
