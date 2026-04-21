package com.norton.backend.seeds;

import com.norton.backend.models.AttendanceStatusModel;
import com.norton.backend.repositories.AttendanceStatusRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Order(6)
public class AttendanceStatusDataLoading implements CommandLineRunner {

  private final AttendanceStatusRepository attendanceStatusRepository;

  @Override
  public void run(String... args) {
    List<AttendanceStatusModel> statuses =
        List.of(
            AttendanceStatusModel.builder()
                .code("PRESENT")
                .name("Present")
                .description("Officer attended on time")
                .build(),
            AttendanceStatusModel.builder()
                .code("LATE")
                .name("Late")
                .description("Officer attended but arrived late")
                .build(),
            AttendanceStatusModel.builder()
                .code("APPROVED")
                .name("Approved")
                .description("Attendance record has been approved")
                .build(),
            AttendanceStatusModel.builder()
                .code("REJECTED")
                .name("Rejected")
                .description("Attendance record has been rejected")
                .build());

    statuses.forEach(this::createIfMissing);
  }

  private void createIfMissing(AttendanceStatusModel status) {
    attendanceStatusRepository
        .findByCode(status.getCode())
        .orElseGet(() -> attendanceStatusRepository.save(status));
  }
}
