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
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn({"officerDataLoading", "attendanceStatusDataLoading"})
@RequiredArgsConstructor
@Order(7)
@Profile("dev")
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

    if (officers.size() >= 5) {
      addIfMissing(
          records,
          plannedKeys,
          officers.get(0).getOfficerCode(),
          LocalDate.of(2026, 4, 14),
          present,
          late,
          approved,
          absent);
      addIfMissing(
          records,
          plannedKeys,
          officers.get(1).getOfficerCode(),
          LocalDate.of(2026, 4, 14),
          present,
          late,
          approved,
          absent);
      addIfMissing(
          records,
          plannedKeys,
          officers.get(4).getOfficerCode(),
          LocalDate.of(2026, 4, 14),
          present,
          late,
          approved,
          absent);
    }

    if (!records.isEmpty()) {
      attendanceRepository.saveAll(records);
    }

    // Ensure today's attendance exists for all officers in the system
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Phnom_Penh"));
    ensureTodaysAttendanceForAllOfficers(officers, today, present, approved);

    System.out.println("Monthly attendance seed data inserted/updated successfully.");
  }

  private void ensureTodaysAttendanceForAllOfficers(
      List<OfficerModel> officers,
      LocalDate date,
      AttendanceStatusModel present,
      AttendanceStatusModel approved) {
    if (officers == null || officers.isEmpty()) {
      return;
    }

    List<AttendanceModel> todays = new ArrayList<>();
    for (OfficerModel officer : officers) {
      if (attendanceRepository.existsByOfficerIdAndDate(officer.getId(), date)) {
        continue;
      }

      LocalDateTime checkIn = date.atTime(8, 0);
      LocalDateTime checkOut = date.atTime(17, 0);
      AttendanceModel a =
          AttendanceModel.builder()
              .officer(officer)
              .date(date)
              .checkIn(checkIn)
              .checkOut(checkOut)
              .totalWorkMin((int) java.time.Duration.between(checkIn, checkOut).toMinutes())
              .totalLateMin(0)
              .status(present != null ? present : approved)
              .notes("Seeded attendance for " + date)
              .build();
      todays.add(a);
    }

    if (!todays.isEmpty()) {
      attendanceRepository.saveAll(todays);
      System.out.println("Inserted today's attendance for " + todays.size() + " officers.");
    }
  }

  private List<OfficerModel> loadSeedOfficers() {
    return new ArrayList<>(
        officerRepository.findAll().stream()
            .sorted(
                Comparator.comparing(
                    OfficerModel::getOfficerCode, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList());
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
    if (officerCode == null || officerCode.isBlank()) {
      return;
    }
    OfficerModel officer = officerRepository.findByOfficerCode(officerCode).orElse(null);
    if (officer == null) {
      return;
    }

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
    if (officerCode == null) return 0;
    try {
      return Math.max(Integer.parseInt(officerCode.replaceAll("\\D", "")) - 1, 0);
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

  private void seedMonthIfMissing(
      List<OfficerModel> officers,
      YearMonth month,
      AttendanceStatusModel present,
      AttendanceStatusModel late,
      AttendanceStatusModel approved,
      AttendanceStatusModel absent) {
    if (officers == null || officers.isEmpty()) {
      return;
    }

    List<AttendanceModel> newRecords = new ArrayList<>();
    for (int officerIndex = 0; officerIndex < officers.size(); officerIndex++) {
      OfficerModel officer = officers.get(officerIndex);
      for (int day = 1; day <= month.lengthOfMonth(); day++) {
        LocalDate date = month.atDay(day);
        if (isWeekend(date)
            || attendanceRepository.existsByOfficerIdAndDate(officer.getId(), date)) {
          continue;
        }
        newRecords.add(
            buildDailyAttendance(officer, officerIndex, date, present, late, approved, absent));
      }
    }

    if (!newRecords.isEmpty()) {
      attendanceRepository.saveAll(newRecords);
      System.out.println(
          "Seeded attendance for month: " + month + " (" + newRecords.size() + " records)");
    }
  }
}
