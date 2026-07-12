package com.norton.backend.seeds;

import com.norton.backend.enums.OfficerStatus;
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

    List<OfficerModel> eligibleOfficers =
        filterEligibleInvitationOfficers(officerRepository.findAll());
    if (eligibleOfficers.isEmpty()) {
      System.out.println("No eligible invitation officers found for seed data.");
      return;
    }

    // --- JANUARY 2026 SEEDS ---
    // 1. New Year Strategy Sync (ACCEPTED)
    InvitationModel strategySync =
        InvitationModel.builder()
            .title("New Year Strategy Sync")
            .description(
                "Strategic session to align all department heads on key directives for 2026.")
            .presidedBy("Director General")
            .eventDate(LocalDate.of(2026, 1, 8))
            .eventTime(LocalTime.of(10, 0))
            .location("Boardroom A")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(strategySync, officer, "ACCEPTED", null);
    }
    invitationRepository.save(strategySync);

    // 2. Q1 Budget Planning Kickoff (ACCEPTED)
    InvitationModel budgetKickoff =
        InvitationModel.builder()
            .title("Q1 Budget Planning Kickoff")
            .description(
                "Collaborative workshop to allocate Q1 resources and review budget templates.")
            .presidedBy("Finance Chairman")
            .eventDate(LocalDate.of(2026, 1, 15))
            .eventTime(LocalTime.of(13, 30))
            .location("Room 102")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(budgetKickoff, officer, "ACCEPTED", null);
    }
    invitationRepository.save(budgetKickoff);

    // 3. Internal Audit Preparation (REJECTED)
    InvitationModel auditPrep =
        InvitationModel.builder()
            .title("Internal Audit Preparation")
            .description(
                "Briefing on documentation and compliance guidelines for the upcoming internal audit.")
            .presidedBy("Chief Auditor")
            .eventDate(LocalDate.of(2026, 1, 22))
            .eventTime(LocalTime.of(9, 30))
            .location("Auditor Office")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(
          auditPrep, officer, "REJECTED", "Scheduled leave for family matters.");
    }
    invitationRepository.save(auditPrep);

    // --- FEBRUARY 2026 SEEDS ---
    // 4. Annual General Assembly 2026 (ACCEPTED)
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
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(assembly, officer, "ACCEPTED", null);
    }
    invitationRepository.save(assembly);

    // 5. Emergency Security Alignment Meeting (PENDING)
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
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(securityMeeting, officer, "PENDING", null);
    }
    invitationRepository.save(securityMeeting);

    // 6. Cross-Department Budget Workshop (REJECTED)
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
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(
          budgetWorkshop,
          officer,
          "REJECTED",
          "Prior commitment to attend the regional health conference.");
    }
    invitationRepository.save(budgetWorkshop);

    // 7. Digital Transformation Seminar (PENDING)
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
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(transformationSeminar, officer, "PENDING", null);
    }
    invitationRepository.save(transformationSeminar);

    // --- MARCH 2026 SEEDS ---
    // 8. Spring Clean Energy Initiative (PENDING)
    InvitationModel energyInitiative =
        InvitationModel.builder()
            .title("Spring Clean Energy Initiative")
            .description(
                "Introduction of green practices and energy-saving measures in the administrative building.")
            .presidedBy("Environment Officer")
            .eventDate(LocalDate.of(2026, 3, 4))
            .eventTime(LocalTime.of(11, 0))
            .location("Main Auditorium")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(energyInitiative, officer, "PENDING", null);
    }
    invitationRepository.save(energyInitiative);

    // 9. Staff Performance Alignment Review (ACCEPTED)
    InvitationModel performanceReview =
        InvitationModel.builder()
            .title("Staff Performance Alignment Review")
            .description(
                "HR briefing to explain the new annual performance metrics and appraisal standards.")
            .presidedBy("HR Director")
            .eventDate(LocalDate.of(2026, 3, 12))
            .eventTime(LocalTime.of(15, 0))
            .location("HR Interview Room")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(performanceReview, officer, "ACCEPTED", null);
    }
    invitationRepository.save(performanceReview);

    // 10. Procurement Standards Update (PENDING)
    InvitationModel procurementUpdate =
        InvitationModel.builder()
            .title("Procurement Standards Update")
            .description(
                "Training on standard procurement bidding protocols and supplier registration guidelines.")
            .presidedBy("Head of Procurement")
            .eventDate(LocalDate.of(2026, 3, 24))
            .eventTime(LocalTime.of(9, 0))
            .location("Conference Room B")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(procurementUpdate, officer, "PENDING", null);
    }
    invitationRepository.save(procurementUpdate);

    // --- JUNE 2026 SEEDS ---
    // 11. Mid-Year Progress Evaluation (ACCEPTED)
    InvitationModel progressEval =
        InvitationModel.builder()
            .title("Mid-Year Progress Evaluation")
            .description(
                "Reviewing performance metrics and progress updates for the first half of 2026.")
            .presidedBy("Director of Administration")
            .eventDate(LocalDate.of(2026, 6, 10))
            .eventTime(LocalTime.of(10, 0))
            .location("Conference Hall B")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(progressEval, officer, "ACCEPTED", null);
    }
    invitationRepository.save(progressEval);

    // 12. General IT Infrastructure Upgrade Briefing (PENDING)
    InvitationModel itBriefing =
        InvitationModel.builder()
            .title("General IT Infrastructure Upgrade Briefing")
            .description(
                "Introduction of security enhancements and system downtime schedules for database migration.")
            .presidedBy("IT Manager")
            .eventDate(LocalDate.of(2026, 6, 25))
            .eventTime(LocalTime.of(14, 0))
            .location("IT Training Room")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(itBriefing, officer, "PENDING", null);
    }
    invitationRepository.save(itBriefing);

    // --- JULY 2026 SEEDS ---
    // 13. Public Relations Workshop (ACCEPTED)
    InvitationModel prWorkshop =
        InvitationModel.builder()
            .title("Public Relations Workshop")
            .description(
                "Practical session on handling public inquiries and media relations for press officers.")
            .presidedBy("PR Department Head")
            .eventDate(LocalDate.of(2026, 7, 8))
            .eventTime(LocalTime.of(9, 30))
            .location("Press Room")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(prWorkshop, officer, "ACCEPTED", null);
    }
    invitationRepository.save(prWorkshop);

    // 14. Anti-Corruption & Compliance Seminar (REJECTED)
    InvitationModel complianceSeminar =
        InvitationModel.builder()
            .title("Anti-Corruption & Compliance Seminar")
            .description(
                "Required annual briefing on ethical practices and legal compliance updates.")
            .presidedBy("Compliance Director")
            .eventDate(LocalDate.of(2026, 7, 22))
            .eventTime(LocalTime.of(14, 0))
            .location("Main Auditorium")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(
          complianceSeminar,
          officer,
          "REJECTED",
          "Already attending the national administrative conference.");
    }
    invitationRepository.save(complianceSeminar);

    // --- AUGUST 2026 SEEDS ---
    // 15. Disaster Management Prep Sync (PENDING)
    InvitationModel disasterSync =
        InvitationModel.builder()
            .title("Disaster Management Prep Sync")
            .description(
                "Emergency coordination meeting on seasonal flood response plans and logistics.")
            .presidedBy("Public Health Officer")
            .eventDate(LocalDate.of(2026, 8, 11))
            .eventTime(LocalTime.of(10, 30))
            .location("Emergency Operation Center")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(disasterSync, officer, "PENDING", null);
    }
    invitationRepository.save(disasterSync);

    // 16. New Employee Orientation Welcoming (ACCEPTED)
    InvitationModel orientationWelcome =
        InvitationModel.builder()
            .title("New Employee Orientation Welcoming")
            .description(
                "Official welcome ceremony and orientation briefing for newly joined administrative staff.")
            .presidedBy("Deputy Governor")
            .eventDate(LocalDate.of(2026, 8, 20))
            .eventTime(LocalTime.of(9, 0))
            .location("Main Lobby Area")
            .type("incoming")
            .status("pending")
            .build();
    for (OfficerModel officer : eligibleOfficers) {
      addParticipantToInvitation(orientationWelcome, officer, "ACCEPTED", null);
    }
    invitationRepository.save(orientationWelcome);

    System.out.println("✅ Invitation seed data loaded successfully!");
  }

  static List<OfficerModel> filterEligibleInvitationOfficers(List<OfficerModel> officers) {
    if (officers == null) {
      return List.of();
    }

    return officers.stream()
        .filter(
            officer ->
                officer != null
                    && officer.getStatus() == OfficerStatus.ACTIVE
                    && Boolean.TRUE.equals(officer.isInvitationPriority()))
        .toList();
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
