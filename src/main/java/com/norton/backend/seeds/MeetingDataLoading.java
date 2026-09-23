package com.norton.backend.seeds;

import com.norton.backend.enums.MeetingStatus;
import com.norton.backend.models.MeetingModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.MeetingRepository;
import com.norton.backend.repositories.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn("userDataLoading")
@RequiredArgsConstructor
@Order(5)
// @Profile("dev")
public class MeetingDataLoading implements CommandLineRunner {

  private final MeetingRepository meetingRepository;
  private final UserRepository userRepository;

  @Override
  public void run(String... args) {
    if (meetingRepository.count() > 0) {
      System.out.println("Meetings already seeded.");
      return;
    }

    List<UserModel> users = userRepository.findAll();
    if (users.isEmpty()) {
      return;
    }

    LocalDate today = LocalDate.now();

    for (UserModel user : users) {
      // Seed a few meetings for each user to populate the mobile dashboard statistics
      meetingRepository.save(
          MeetingModel.builder()
              .title("Project Kickoff - " + user.getFullName())
              .meetingDate(today)
              .meetingTime(LocalTime.of(9, 30))
              .status(MeetingStatus.COMPLETED)
              .assigneeId(user.getId())
              .build());

      meetingRepository.save(
          MeetingModel.builder()
              .title("Weekly Sync - " + user.getFullName())
              .meetingDate(today)
              .meetingTime(LocalTime.of(14, 0))
              .status(MeetingStatus.PENDING)
              .assigneeId(user.getId())
              .build());

      meetingRepository.save(
          MeetingModel.builder()
              .title("Budget Review - " + user.getFullName())
              .meetingDate(today)
              .meetingTime(LocalTime.of(16, 30))
              .status(MeetingStatus.POSTPONED)
              .assigneeId(user.getId())
              .build());

      meetingRepository.save(
          MeetingModel.builder()
              .title("Upcoming General Assembly")
              .meetingDate(today.plusDays(1))
              .meetingTime(LocalTime.of(10, 0))
              .status(MeetingStatus.PENDING)
              .assigneeId(user.getId())
              .build());

      meetingRepository.save(
          MeetingModel.builder()
              .title("Cancelled Strategy Session")
              .meetingDate(today.minusDays(1))
              .meetingTime(LocalTime.of(11, 0))
              .status(MeetingStatus.CANCELLED)
              .assigneeId(user.getId())
              .build());
    }

    System.out.println("✅ Meeting seed data loaded successfully!");
  }
}
