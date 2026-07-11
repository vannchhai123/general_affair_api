package com.norton.backend.seeds;

import com.norton.backend.models.InvitationModel;
import com.norton.backend.models.InvitationParticipantModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.InvitationRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn("officerDataLoading")
@RequiredArgsConstructor
@Order(9)
public class InvitationDataLoading implements CommandLineRunner {

  private final InvitationRepository invitationRepository;
  private final OfficerRepository officerRepository;

  @Override
  public void run(String... args) {
    if (invitationRepository.count() > 0) {
      System.out.println("Invitations already seeded.");
      return;
    }

    List<OfficerModel> officers = officerRepository.findAll();
    if (officers.isEmpty()) {
      return;
    }

    // 1. Annual General Assembly 2026 (ACCEPTED)
    InvitationModel assembly =
        InvitationModel.builder()
            .title("Annual General Assembly 2026")
            .description("Annual assembly to review last year's performance and plan for 2026.")
            .presidedBy("Governor of Phnom Penh")
            .eventDate(LocalDate.of(2026, 2, 5))
            .eventTime(LocalTime.of(9, 0))
            .location("Main Conference Hall")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : officers) {
      addParticipantToInvitation(assembly, officer, "ACCEPTED", null);
    }
    invitationRepository.save(assembly);

    // 2. Emergency Security Alignment Meeting (PENDING)
    InvitationModel securityMeeting =
        InvitationModel.builder()
            .title("Emergency Security Alignment Meeting")
            .description("Urgent meeting to align security measures across all departments.")
            .presidedBy("Director of Administration")
            .eventDate(LocalDate.of(2026, 2, 12))
            .eventTime(LocalTime.of(14, 30))
            .location("Meeting Room 1 (Floor 2)")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : officers) {
      addParticipantToInvitation(securityMeeting, officer, "PENDING", null);
    }
    invitationRepository.save(securityMeeting);

    // 3. Cross-Department Budget Workshop (REJECTED)
    InvitationModel budgetWorkshop =
        InvitationModel.builder()
            .title("Cross-Department Budget Workshop")
            .description("Reviewing Q1 budget utilization and matching resource allocations.")
            .presidedBy("Finance Chairman")
            .eventDate(LocalDate.of(2026, 2, 18))
            .eventTime(LocalTime.of(10, 0))
            .location("Room 405 (Finance Block)")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : officers) {
      addParticipantToInvitation(
          budgetWorkshop,
          officer,
          "REJECTED",
          "Prior commitment to attend the regional health conference.");
    }
    invitationRepository.save(budgetWorkshop);

    // 4. Digital Transformation Seminar (PENDING)
    InvitationModel transformationSeminar =
        InvitationModel.builder()
            .title("Digital Transformation Seminar")
            .description(
                "Training session on the newly introduced general affairs digital filing system.")
            .presidedBy("IT Department Head")
            .eventDate(LocalDate.of(2026, 2, 25))
            .eventTime(LocalTime.of(8, 30))
            .location("Training Room C")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : officers) {
      addParticipantToInvitation(transformationSeminar, officer, "PENDING", null);
    }
    invitationRepository.save(transformationSeminar);

    System.out.println("✅ Invitation seed data loaded successfully!");
  }

  private void addParticipantToInvitation(
      InvitationModel invitation, OfficerModel officer, String status, String rejectionReason) {
    InvitationParticipantModel participant =
        InvitationParticipantModel.builder()
            .invitation(invitation)
            .officer(officer)
            .status(status)
            .rejectionReason(rejectionReason)
            .build();
    invitation.getParticipants().add(participant);
  }
}
