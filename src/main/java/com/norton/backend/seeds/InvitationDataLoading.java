package com.norton.backend.seeds;

import com.norton.backend.models.InvitationModel;
import com.norton.backend.models.InvitationParticipantModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.InvitationRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn("officerDataLoading")
@RequiredArgsConstructor
@Order(9)
@Profile("dev")
public class InvitationDataLoading implements CommandLineRunner {

  private final InvitationRepository invitationRepository;
  private final OfficerRepository officerRepository;
  private final Random random = new Random(42);

  private static final String[] TYPES = {"incoming", "outgoing"};
  private static final String[] PARTICIPANT_STATUSES = {"ACCEPTED", "PENDING", "REJECTED"};

  private static final String[] REJECTION_REASONS = {
    "Scheduled leave for family matters.",
    "Prior commitment to attend the regional health conference.",
    "Participating in official field mission outside Phnom Penh.",
    "Urgent departmental operational task required on-site.",
    "Already attending another high-priority executive summit."
  };

  private static final String[] LOCATIONS = {
    "Main Conference Hall",
    "Boardroom A",
    "VIP Reception Hall",
    "Convention Center Hall A",
    "Meeting Room 1 (Floor 2)",
    "Training Room C",
    "Main Auditorium",
    "Emergency Operation Center",
    "Press Room",
    "Auditorium 2",
    "Room 405 (Finance Block)",
    "Sen Sok Executive Hall"
  };

  private static final String[] PRESIDED_BY = {
    "Director General",
    "Finance Chairman",
    "Chief Auditor",
    "Governor of Phnom Penh",
    "Director of Administration",
    "IT Department Head",
    "Secretary of State",
    "Deputy Governor",
    "Head of General Affairs",
    "Health & Safety Director"
  };

  @Override
  public void run(String... args) {
    invitationRepository.deleteAll();

    List<OfficerModel> allOfficers = officerRepository.findAll();
    if (allOfficers.isEmpty()) {
      System.out.println("⚠️ No officers found to seed invitations.");
      return;
    }

    List<InvitationSeedItem> seedItems = createSeedCatalog();

    for (InvitationSeedItem item : seedItems) {
      String assignedType = TYPES[random.nextInt(TYPES.length)];
      String location = LOCATIONS[random.nextInt(LOCATIONS.length)];
      String presidedBy = PRESIDED_BY[random.nextInt(PRESIDED_BY.length)];

      InvitationModel invitation =
          InvitationModel.builder()
              .title(item.title)
              .description(item.description)
              .presidedBy(presidedBy)
              .eventDate(item.eventDate)
              .eventTime(item.eventTime)
              .location(location)
              .type(assignedType)
              .status("pending")
              .participants(new ArrayList<>())
              .build();

      for (OfficerModel officer : allOfficers) {
        String pStatus = PARTICIPANT_STATUSES[random.nextInt(PARTICIPANT_STATUSES.length)];
        String rejectionReason =
            "REJECTED".equals(pStatus)
                ? REJECTION_REASONS[random.nextInt(REJECTION_REASONS.length)]
                : null;

        InvitationParticipantModel participant =
            InvitationParticipantModel.builder()
                .invitation(invitation)
                .officer(officer)
                .status(pStatus)
                .rejectionReason(rejectionReason)
                .build();

        invitation.getParticipants().add(participant);
      }

      invitationRepository.save(invitation);
    }

    System.out.println(
        "✅ "
            + seedItems.size()
            + " Invitation seed records with randomized incoming/outgoing types loaded successfully!");
  }

  private record InvitationSeedItem(
      String title, String description, LocalDate eventDate, LocalTime eventTime) {}

  private List<InvitationSeedItem> createSeedCatalog() {
    List<InvitationSeedItem> items = new ArrayList<>();

    // --- August 2026 (Rich focus) ---
    items.add(
        new InvitationSeedItem(
            "Ministry Delegation Joint Reception",
            "Official reception and bilateral meeting with delegates from the Ministry of Public Functions.",
            LocalDate.of(2026, 8, 3),
            LocalTime.of(8, 30)));
    items.add(
        new InvitationSeedItem(
            "Disaster Management Prep Sync",
            "Emergency coordination meeting on seasonal flood response plans and logistics.",
            LocalDate.of(2026, 8, 7),
            LocalTime.of(10, 30)));
    items.add(
        new InvitationSeedItem(
            "Smart City Digital Governance Summit",
            "Conference discussing AI governance and public service digital acceleration.",
            LocalDate.of(2026, 8, 12),
            LocalTime.of(14, 0)));
    items.add(
        new InvitationSeedItem(
            "Q3 Inter-Departmental Collaboration Workshop",
            "Aligning regional operations and cross-functional team initiatives.",
            LocalDate.of(2026, 8, 15),
            LocalTime.of(9, 30)));
    items.add(
        new InvitationSeedItem(
            "New Employee Orientation Welcoming",
            "Official welcome ceremony and orientation briefing for newly joined staff.",
            LocalDate.of(2026, 8, 20),
            LocalTime.of(9, 0)));
    items.add(
        new InvitationSeedItem(
            "Executive Committee Policy Review",
            "Monthly evaluation of administrative policies and compliance metrics.",
            LocalDate.of(2026, 8, 22),
            LocalTime.of(15, 0)));
    items.add(
        new InvitationSeedItem(
            "General Affairs Quarter 3 Review",
            "Quarterly review of administrative workflow and equipment procurement.",
            LocalDate.of(2026, 8, 25),
            LocalTime.of(10, 0)));
    items.add(
        new InvitationSeedItem(
            "Annual Staff Health & Safety Workshop",
            "Annual workplace safety compliance and health insurance orientation.",
            LocalDate.of(2026, 8, 28),
            LocalTime.of(13, 30)));
    items.add(
        new InvitationSeedItem(
            "Public Service Innovation Forum",
            "Showcase of digital transformation prototypes and citizen-centric workflows.",
            LocalDate.of(2026, 8, 31),
            LocalTime.of(11, 0)));

    // --- July 2026 ---
    items.add(
        new InvitationSeedItem(
            "Public Relations Strategy Workshop",
            "Practical session on handling public inquiries and media relations.",
            LocalDate.of(2026, 7, 8),
            LocalTime.of(9, 30)));
    items.add(
        new InvitationSeedItem(
            "Anti-Corruption & Compliance Seminar",
            "Required annual briefing on ethical practices and legal compliance updates.",
            LocalDate.of(2026, 7, 22),
            LocalTime.of(14, 0)));
    items.add(
        new InvitationSeedItem(
            "Mid-Year Financial Performance Assessment",
            "Review of budget allocation and department spending reports.",
            LocalDate.of(2026, 7, 29),
            LocalTime.of(10, 0)));

    // --- June 2026 ---
    items.add(
        new InvitationSeedItem(
            "Mid-Year Progress Evaluation",
            "Reviewing performance metrics and progress updates for the first half of 2026.",
            LocalDate.of(2026, 6, 10),
            LocalTime.of(10, 0)));
    items.add(
        new InvitationSeedItem(
            "General IT Infrastructure Upgrade Briefing",
            "Introduction of security enhancements and system downtime schedules.",
            LocalDate.of(2026, 6, 25),
            LocalTime.of(14, 0)));

    // --- May 2026 ---
    items.add(
        new InvitationSeedItem(
            "Labor Standards & Employee Welfare Summit",
            "Discussion on workplace ethics, officer safety standards, and insurance perks.",
            LocalDate.of(2026, 5, 12),
            LocalTime.of(9, 0)));
    items.add(
        new InvitationSeedItem(
            "Records Management System Training",
            "Hands-on training session on electronic document archiving and logging.",
            LocalDate.of(2026, 5, 20),
            LocalTime.of(13, 30)));

    // --- April 2026 ---
    items.add(
        new InvitationSeedItem(
            "Khmer New Year Administrative Gathering",
            "Annual staff celebration and seasonal administrative schedule announcement.",
            LocalDate.of(2026, 4, 10),
            LocalTime.of(10, 0)));
    items.add(
        new InvitationSeedItem(
            "Post-Holiday Operational Alignment",
            "Resuming full operational duties and checking project milestones.",
            LocalDate.of(2026, 4, 21),
            LocalTime.of(8, 30)));

    // --- March 2026 ---
    items.add(
        new InvitationSeedItem(
            "Spring Clean Energy Initiative",
            "Introduction of green practices and energy-saving measures in government buildings.",
            LocalDate.of(2026, 3, 4),
            LocalTime.of(11, 0)));
    items.add(
        new InvitationSeedItem(
            "Staff Performance Alignment Review",
            "HR briefing to explain the new annual performance metrics and appraisal standards.",
            LocalDate.of(2026, 3, 12),
            LocalTime.of(15, 0)));
    items.add(
        new InvitationSeedItem(
            "Procurement Standards Update",
            "Training on standard procurement bidding protocols and supplier registration.",
            LocalDate.of(2026, 3, 24),
            LocalTime.of(9, 0)));

    // --- February 2026 ---
    items.add(
        new InvitationSeedItem(
            "Annual General Assembly 2026",
            "Annual assembly to review last year's performance and plan for 2026.",
            LocalDate.of(2026, 2, 5),
            LocalTime.of(9, 0)));
    items.add(
        new InvitationSeedItem(
            "Emergency Security Alignment Meeting",
            "Urgent meeting to align security measures across all departments.",
            LocalDate.of(2026, 2, 12),
            LocalTime.of(14, 30)));
    items.add(
        new InvitationSeedItem(
            "Cross-Department Budget Workshop",
            "Reviewing Q1 budget utilization and matching resource allocations.",
            LocalDate.of(2026, 2, 18),
            LocalTime.of(10, 0)));
    items.add(
        new InvitationSeedItem(
            "Digital Transformation Seminar",
            "Training session on the newly introduced general affairs digital filing system.",
            LocalDate.of(2026, 2, 25),
            LocalTime.of(8, 30)));

    // --- January 2026 ---
    items.add(
        new InvitationSeedItem(
            "New Year Strategy Sync",
            "Strategic session to align all department heads on key directives for 2026.",
            LocalDate.of(2026, 1, 8),
            LocalTime.of(10, 0)));
    items.add(
        new InvitationSeedItem(
            "Q1 Budget Planning Kickoff",
            "Collaborative workshop to allocate Q1 resources and review budget templates.",
            LocalDate.of(2026, 1, 15),
            LocalTime.of(13, 30)));
    items.add(
        new InvitationSeedItem(
            "Internal Audit Preparation",
            "Briefing on documentation and compliance guidelines for the upcoming audit.",
            LocalDate.of(2026, 1, 22),
            LocalTime.of(9, 30)));

    return items;
  }
}
