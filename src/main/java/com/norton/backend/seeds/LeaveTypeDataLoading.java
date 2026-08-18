package com.norton.backend.seeds;

import com.norton.backend.models.LeaveTypeModel;
import com.norton.backend.repositories.LeaveTypeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component("leaveTypeDataLoading")
@RequiredArgsConstructor
@Order(11)
// @Profile("dev")
public class LeaveTypeDataLoading implements CommandLineRunner {

  private final LeaveTypeRepository leaveTypeRepository;

  @Override
  public void run(String... args) {
    if (leaveTypeRepository.count() > 0) {
      log.info("Leave types database already populated. Skipping seeding.");
      return;
    }

    log.info("Seeding initial Leave Types data...");

    List<LeaveTypeModel> types =
        List.of(
            LeaveTypeModel.builder()
                .key("Annual Leave")
                .labelEn("Annual Leave")
                .labelKh("ច្បាប់សម្រាកប្រចាំឆ្នាំ")
                .description("Standard annual paid leave allocation")
                .isActive(true)
                .build(),
            LeaveTypeModel.builder()
                .key("Sick Leave")
                .labelEn("Sick Leave")
                .labelKh("ច្បាប់ជំងឺ")
                .description("Leave taken due to medical or health conditions")
                .isActive(true)
                .build(),
            LeaveTypeModel.builder()
                .key("Personal Leave")
                .labelEn("Personal Leave")
                .labelKh("ច្បាប់ផ្ទាល់ខ្លួន")
                .description("Leave for urgent personal affairs and family business")
                .isActive(true)
                .build(),
            LeaveTypeModel.builder()
                .key("Special Leave")
                .labelEn("Special Leave")
                .labelKh("ច្បាប់ពិសេស")
                .description("Special leave for weddings, events, or authorized activities")
                .isActive(true)
                .build(),
            LeaveTypeModel.builder()
                .key("Maternity Leave")
                .labelEn("Maternity / Paternity Leave")
                .labelKh("ច្បាប់មាតុភាព / បិតុភាព")
                .description("Parental leave for newborn care")
                .isActive(true)
                .build());

    leaveTypeRepository.saveAll(types);
    log.info("Leave Types seed data created successfully! ({}) types created.", types.size());
  }
}
