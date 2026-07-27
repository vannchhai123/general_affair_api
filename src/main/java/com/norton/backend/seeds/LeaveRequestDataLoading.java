package com.norton.backend.seeds;

import com.norton.backend.models.LeaveRequestModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.LeaveRequestRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DependsOn("officerDataLoading")
@RequiredArgsConstructor
@Order(12)
public class LeaveRequestDataLoading implements CommandLineRunner {

  private final LeaveRequestRepository leaveRequestRepository;
  private final OfficerRepository officerRepository;

  @Override
  public void run(String... args) {
    if (leaveRequestRepository.count() > 0) {
      log.info("Leave requests database already populated. Skipping seeding.");
      return;
    }

    List<OfficerModel> officers = officerRepository.findAll();
    if (officers.isEmpty()) {
      log.warn("No officers found for seeding leave requests.");
      return;
    }

    log.info("Seeding initial Leave Requests data...");

    OfficerModel officer1 = officers.get(0);
    OfficerModel officer2 = officers.size() > 1 ? officers.get(1) : officer1;
    OfficerModel officer3 = officers.size() > 2 ? officers.get(2) : officer1;
    OfficerModel officer4 = officers.size() > 3 ? officers.get(3) : officer1;

    LeaveRequestModel leave1 =
        LeaveRequestModel.builder()
            .officer(officer1)
            .leaveType("Annual Leave")
            .startDate(LocalDate.now().plusDays(2))
            .endDate(LocalDate.now().plusDays(4))
            .totalDays(3)
            .reason("សុំច្បាប់សម្រាកប្រចាំឆ្នាំជាមួយគ្រួសារ (Annual Leave)")
            .status("Pending")
            .build();

    LeaveRequestModel leave2 =
        LeaveRequestModel.builder()
            .officer(officer2)
            .leaveType("Sick Leave")
            .startDate(LocalDate.now().minusDays(3))
            .endDate(LocalDate.now().minusDays(1))
            .totalDays(3)
            .reason("សុំច្បាប់ព្យាបាលជំងឺ (Medical & Sick Leave)")
            .status("Approved")
            .approvedByOfficer(officer1)
            .approvedAt(LocalDateTime.now().minusDays(3))
            .build();

    LeaveRequestModel leave3 =
        LeaveRequestModel.builder()
            .officer(officer3)
            .leaveType("Personal Leave")
            .startDate(LocalDate.now().minusDays(5))
            .endDate(LocalDate.now().minusDays(5))
            .totalDays(1)
            .reason("សុំច្បាប់ធុរៈផ្ទាល់ខ្លួន (Personal Family Matter)")
            .status("Rejected")
            .approvedByOfficer(officer1)
            .approvedAt(LocalDateTime.now().minusDays(5))
            .build();

    LeaveRequestModel leave4 =
        LeaveRequestModel.builder()
            .officer(officer4)
            .leaveType("Special Leave")
            .startDate(LocalDate.now().plusDays(5))
            .endDate(LocalDate.now().plusDays(7))
            .totalDays(3)
            .reason("សុំច្បាប់ចូលរួមពិធីអាពាហ៍ពិពាហ៍ (Special Leave for Wedding Event)")
            .status("Pending")
            .build();

    leaveRequestRepository.saveAll(List.of(leave1, leave2, leave3, leave4));
    log.info("Leave Requests seed data created successfully!");
  }
}
