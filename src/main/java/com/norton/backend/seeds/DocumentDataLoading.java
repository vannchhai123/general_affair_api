package com.norton.backend.seeds;

import com.norton.backend.models.*;
import com.norton.backend.repositories.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn("officerDataLoading")
@RequiredArgsConstructor
@Order(6)
public class DocumentDataLoading implements CommandLineRunner {

  private final DocumentTypeRepository documentTypeRepository;
  private final OrganizationRepository organizationRepository;
  private final DocumentRepository documentRepository;
  private final DocumentFileRepository documentFileRepository;
  private final DocumentLogRepository documentLogRepository;
  private final OfficerRepository officerRepository;
  private final UploadImageRepository uploadImageRepository;

  @Override
  public void run(String... args) {
    if (documentTypeRepository.count() > 0) {
      System.out.println("Document management seed data already loaded.");
      return;
    }

    List<OfficerModel> officers = officerRepository.findAll();
    if (officers.isEmpty()) {
      System.out.println("⚠️ No officers found to associate with documents. Skipping seeding.");
      return;
    }

    OfficerModel adminOfficer = officers.get(0);
    OfficerModel otherOfficer = officers.size() > 1 ? officers.get(1) : adminOfficer;

    // 1. Seed Document Types
    List<DocumentTypeModel> types = new ArrayList<>();
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("លិខិតស្នើ")
                .code("REQ_LTR")
                .description("លិខិតស្នើ (Request Letter)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("សេចក្តីជូនដំណឹង")
                .code("NOTICE")
                .description("សេចក្តីជូនដំណឹង (Notice / Announcement)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("លិខិតអញ្ជើញ")
                .code("INV_LTR")
                .description("លិខិតអញ្ជើញ (Invitation Letter)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("លិខិតបញ្ជាបេសកកម្ម")
                .code("MSN_ORD")
                .description("លិខិតបញ្ជាបេសកកម្ម (Mission Order)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("លិខិតចុះទិដ្ឋាការធ្វើដំណើរ")
                .code("TRV_VISA")
                .description("លិខិតចុះទិដ្ឋាការធ្វើដំណើរ (Travel Visa/Permit)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("លិខិតផ្ទេរសិទ្ធិ")
                .code("DEL_AUTH")
                .description("លិខិតផ្ទេរសិទ្ធិ (Delegation of Authority)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("លិខិតប្រគល់សិទ្ធិចុះហត្ថលេខា")
                .code("POA_SIGN")
                .description("លិខិតប្រគល់សិទ្ធិចុះហត្ថលេខា (Power of Attorney to Sign)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("ដីកាអម")
                .code("COV_LTR")
                .description("ដីកាអម (Covering Letter)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("លិខិតអនុញ្ញាតច្បាប់ឈប់សម្រាក")
                .code("LV_PERM")
                .description("លិខិតអនុញ្ញាតច្បាប់ឈប់សម្រាក (Leave Permission)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("កំណត់បង្ហាញរឿង")
                .code("CASE_MEMO")
                .description("កំណត់បង្ហាញរឿង (Case Presentation)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("របាយការណ៍")
                .code("REPORT")
                .description("របាយការណ៍ (Report)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("កំណត់ហេតុ")
                .code("MINUTES")
                .description("កំណត់ហេតុ (Minutes / Record)")
                .build()));
    types.add(
        documentTypeRepository.save(
            DocumentTypeModel.builder()
                .name("លិខិតបញ្ជាក់")
                .code("CERT_LTR")
                .description("លិខិតបញ្ជាក់ (Certification Letter)")
                .build()));

    // 2. Seed Organizations
    List<OrganizationModel> orgs = new ArrayList<>();
    orgs.add(
        organizationRepository.save(
            OrganizationModel.builder()
                .name("Ministry of Interior")
                .shortName("MoI")
                .organizationType("Government")
                .phone("023-721-901")
                .email("info@moi.gov.kh")
                .address("Preah Norodom Blvd, Phnom Penh")
                .website("https://www.moi.gov.kh")
                .status("ACTIVE")
                .build()));
    orgs.add(
        organizationRepository.save(
            OrganizationModel.builder()
                .name("Ministry of Foreign Affairs")
                .shortName("MFAIC")
                .organizationType("Government")
                .phone("023-216-122")
                .email("mfaic@mfa.gov.kh")
                .address("Samdech Hun Sen Street, Phnom Penh")
                .website("https://www.mfaic.gov.kh")
                .status("ACTIVE")
                .build()));
    orgs.add(
        organizationRepository.save(
            OrganizationModel.builder()
                .name("Norton University")
                .shortName("NU")
                .organizationType("Education")
                .phone("023-432-100")
                .email("info@norton.edu.kh")
                .address("Chroy Changvar, Phnom Penh")
                .website("https://www.norton.edu.kh")
                .status("ACTIVE")
                .build()));
    orgs.add(
        organizationRepository.save(
            OrganizationModel.builder()
                .name("Ministry of Education, Youth and Sport")
                .shortName("MoEYS")
                .organizationType("Government")
                .phone("023-210-134")
                .email("info@moeys.gov.kh")
                .address("Preah Norodom Blvd, Phnom Penh")
                .website("https://www.moeys.gov.kh")
                .status("ACTIVE")
                .build()));

    // 4. Seed Upload Image for files association
    UploadImageModel dummyImage =
        uploadImageRepository.save(
            UploadImageModel.builder()
                .fileName("official_document_sample.pdf")
                .url("https://example.com/storage/official_document_sample.pdf")
                .build());

    // 5. Seed Documents
    LocalDate today = LocalDate.now();

    // Document 1: Incoming Official Letter
    DocumentModel doc1 =
        DocumentModel.builder()
            .direction("INCOMING")
            .documentType(types.get(0)) // Official Letter
            .senderOrganization(orgs.get(0)) // MoI
            .receiverOrganization(orgs.get(2)) // NU
            .documentNumber("MoI-2026-1049")
            .documentDate(today.minusDays(5))
            .receivedDate(today.minusDays(4))
            .subject("Invitation to Security Awareness Workshop")
            .summary(
                "Invitation for Norton University representatives to attend the national security workshop next month.")
            .confidentiality("NORMAL")
            .priority("HIGH")
            .status("RECEIVED")
            .remarks("Assigned to IT Admin for coordination")
            .createdBy(adminOfficer)
            .build();
    doc1 = documentRepository.save(doc1);

    // Save File for Doc 1
    documentFileRepository.save(
        DocumentFileModel.builder()
            .document(doc1)
            .uploadImage(dummyImage)
            .fileName("MoI-2026-1049_Security_Workshop.pdf")
            .filePath("/documents/2026/07/MoI-2026-1049_Security_Workshop.pdf")
            .mimeType("application/pdf")
            .fileSize(1024567L)
            .isPrimary(true)
            .uploadedBy(adminOfficer)
            .build());

    // Save Logs for Doc 1
    documentLogRepository.save(
        DocumentLogModel.builder()
            .document(doc1)
            .officer(adminOfficer)
            .action("CREATE")
            .description("Document imported and registered in general affair system.")
            .build());

    // Document 2: Outgoing Circular
    DocumentModel doc2 =
        DocumentModel.builder()
            .direction("OUTGOING")
            .documentType(types.get(1)) // Circular
            .senderOrganization(orgs.get(2)) // NU
            .receiverOrganization(orgs.get(3)) // MoEYS
            .documentNumber("NU-CIR-2026-088")
            .documentDate(today.minusDays(2))
            .subject("Implementation of E-learning Standards")
            .summary(
                "Circular informing MoEYS about the implementation and compliance to online academic guidelines.")
            .confidentiality("NORMAL")
            .priority("NORMAL")
            .status("SENT")
            .remarks("Delivered via email and hardcopy")
            .createdBy(adminOfficer)
            .updatedBy(otherOfficer)
            .build();
    doc2 = documentRepository.save(doc2);

    // Save File for Doc 2
    documentFileRepository.save(
        DocumentFileModel.builder()
            .document(doc2)
            .uploadImage(dummyImage)
            .fileName("NU-CIR-2026-088_Elearning_Report.pdf")
            .filePath("/documents/2026/07/NU-CIR-2026-088_Elearning_Report.pdf")
            .mimeType("application/pdf")
            .fileSize(2048991L)
            .isPrimary(true)
            .uploadedBy(otherOfficer)
            .build());

    // Save Logs for Doc 2
    documentLogRepository.save(
        DocumentLogModel.builder()
            .document(doc2)
            .officer(adminOfficer)
            .action("CREATE")
            .description("Circular document draft initialized.")
            .build());

    documentLogRepository.save(
        DocumentLogModel.builder()
            .document(doc2)
            .officer(otherOfficer)
            .action("UPDATE")
            .description("Circular content approved and marked as SENT.")
            .build());

    // Document 3: Confidential Internal Memo
    DocumentModel doc3 =
        DocumentModel.builder()
            .direction("INTERNAL") // We use INTERNAL direction
            .documentType(types.get(2)) // Memo
            .senderOrganization(orgs.get(2)) // NU
            .receiverOrganization(orgs.get(2)) // NU
            .documentNumber("NU-MEMO-IT-004")
            .documentDate(today)
            .subject("Confidential IT Infrastructure Upgrades Budget")
            .summary(
                "Budget allocation proposal and hardware specifications details for server migration.")
            .confidentiality("CONFIDENTIAL")
            .priority("CRITICAL")
            .status("DRAFT")
            .remarks("Requires review by executive committee")
            .createdBy(otherOfficer)
            .build();
    doc3 = documentRepository.save(doc3);

    // Save Logs for Doc 3
    documentLogRepository.save(
        DocumentLogModel.builder()
            .document(doc3)
            .officer(otherOfficer)
            .action("CREATE")
            .description("Confidential IT memo draft created by Officer.")
            .build());

    System.out.println("✅ Document Management seed data loaded successfully!");
  }
}
