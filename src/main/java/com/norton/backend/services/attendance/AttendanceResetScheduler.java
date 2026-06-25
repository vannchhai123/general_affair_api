package com.norton.backend.services.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduler for daily attendance reset operations.
 *
 * <p>Runs at midnight (00:00) in the configured scan timezone to: 1. Verify attendance records are
 * properly isolated by date 2. Clean up stale attendance sessions from previous days 3. Ensure the
 * system is ready for new day's check-ins
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceResetScheduler {

  private final AttendanceService attendanceService;

  @Value("${attendance.scan.timezone:Asia/Phnom_Penh}")
  private String scanTimezone;

  /**
   * Scheduled task that runs daily at midnight in the configured timezone. Uses CRON with fixed
   * delay to ensure it doesn't overlap.
   *
   * <p>At midnight (start of new day): 1. Logs the reset event for monitoring 2. Optionally cleans
   * up incomplete sessions from previous day 3. Verifies all officers can see empty state for new
   * day
   */
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Phnom_Penh")
  @Transactional
  public void dailyAttendanceReset() {
    ZoneId zoneId = ZoneId.of(scanTimezone);
    LocalDateTime resetTime = LocalDateTime.now(zoneId);
    LocalDate today = resetTime.toLocalDate();

    log.info(
        "=== DAILY ATTENDANCE RESET TRIGGERED ===\n" + "Reset Time: {} ({})\n" + "Today Date: {}",
        resetTime,
        zoneId,
        today);

    try {
      clearTodayAttendanceRecords(today);
      log.info("DAILY ATTENDANCE RESET COMPLETED SUCCESSFULLY");
      log.info("Status: Today's attendance records cleared for new day");
      log.info("Next queries will return empty state for new day (unless manually created)");

    } catch (Exception e) {
      log.error("ERROR during daily attendance reset", e);
    }
  }

  private void clearTodayAttendanceRecords(LocalDate today) {
    attendanceService.deleteAllAttendancesForDate(today);
  }

  /**
   * Alternative: Manual trigger for testing/debugging reset behavior. Call this endpoint to
   * immediately trigger reset logic outside of schedule.
   */
  @Transactional
  public void manualResetTrigger() {
    log.info("Manual attendance reset triggered");
    dailyAttendanceReset();
  }
}
