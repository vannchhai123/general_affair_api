package com.norton.backend.seeds;

import com.norton.backend.config.FileStorageProperties;
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
  private final FileStorageProperties fileStorageProperties;

  @Override
  public void run(String... args) {
    List<OfficerModel> officers = officerRepository.findAll();
    if (officers.isEmpty()) {
      System.out.println("⚠️ No officers found to associate with documents. Skipping seeding.");
      return;
    }

    OfficerModel adminOfficer = officers.get(0);
    OfficerModel otherOfficer = officers.size() > 1 ? officers.get(1) : adminOfficer;

    // 1. Seed Document Types
    List<DocumentTypeModel> types = new ArrayList<>();
    if (documentTypeRepository.count() == 0) {
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
    } else {
      types = documentTypeRepository.findAll();
    }

    // 2. Seed Organizations
    List<OrganizationModel> orgs = new ArrayList<>();
    if (organizationRepository.count() == 0) {
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
    } else {
      orgs = organizationRepository.findAll();
    }

    if (orgs.isEmpty()) {
      System.out.println("⚠️ No organizations found. Skipping document seeding.");
      return;
    }

    // 4. Seed Upload Image for files association
    UploadImageModel dummyImage;
    List<UploadImageModel> images = uploadImageRepository.findAll();
    if (images.isEmpty()) {
      dummyImage =
          uploadImageRepository.save(
              UploadImageModel.builder()
                  .fileName("official_document_sample.pdf")
                  .url(fileStorageProperties.getBaseUrl() + "/official_document_sample.pdf")
                  .build());
    } else {
      dummyImage = images.get(0);
      // Let's ensure the dummy image has the correct URL in dev
      if (dummyImage.getUrl().contains("example.com")) {
        dummyImage.setUrl(fileStorageProperties.getBaseUrl() + "/official_document_sample.pdf");
        dummyImage = uploadImageRepository.save(dummyImage);
      }
    }

    // Check if documents are already seeded
    if (documentRepository.count() > 0) {
      System.out.println("Document seed data already loaded.");
      return;
    }

    // 5. Seed Documents (20 documents per document type)
    LocalDate today = LocalDate.now();
    OrganizationModel nuOrg =
        orgs.stream()
            .filter(o -> "NU".equalsIgnoreCase(o.getShortName()))
            .findFirst()
            .orElse(orgs.get(0));

    List<OrganizationModel> externalOrgs =
        orgs.stream().filter(o -> !"NU".equalsIgnoreCase(o.getShortName())).toList();
    if (externalOrgs.isEmpty()) {
      externalOrgs = orgs;
    }

    int totalDocsSeeded = 0;
    for (DocumentTypeModel type : types) {
      for (int i = 1; i <= 20; i++) {
        // Determine direction
        String direction;
        if (i <= 7) {
          direction = "INCOMING";
        } else if (i <= 14) {
          direction = "OUTGOING";
        } else {
          direction = "INTERNAL";
        }

        // Determine sender and receiver organizations based on direction
        OrganizationModel sender;
        OrganizationModel receiver;
        if ("INCOMING".equals(direction)) {
          sender = externalOrgs.get(i % externalOrgs.size());
          receiver = nuOrg;
        } else if ("OUTGOING".equals(direction)) {
          sender = nuOrg;
          receiver = externalOrgs.get(i % externalOrgs.size());
        } else {
          sender = nuOrg;
          receiver = nuOrg;
        }

        // Generate attributes
        String confidentiality = (i % 5 == 0) ? "CONFIDENTIAL" : "NORMAL";
        String priority =
            (i % 5 == 0) ? "CRITICAL" : ((i % 5 == 1 || i % 5 == 2) ? "HIGH" : "NORMAL");
        String status = (i % 3 == 0) ? "LOGGED" : ((i % 3 == 1) ? "PENDING" : "APPROVED");
        LocalDate docDate = today.minusDays(i);
        LocalDate receivedDate = "INCOMING".equals(direction) ? docDate.plusDays(1) : null;
        String docNum = type.getCode() + "-2026-" + String.format("%04d", i);

        String subject;
        String summary;
        if ("INCOMING".equals(direction)) {
          subject = "Incoming " + type.getName() + " regarding collaboration - Vol. " + i;
          summary =
              "Received official "
                  + type.getDescription()
                  + " from "
                  + sender.getName()
                  + " regarding implementation guidelines and cooperation setup. Assigned to admin.";
        } else if ("OUTGOING".equals(direction)) {
          subject = "Outgoing " + type.getName() + " on official response - Vol. " + i;
          summary =
              "Dispatched "
                  + type.getDescription()
                  + " to "
                  + receiver.getName()
                  + " outlining Norton University's compliance checklist and feedback report.";
        } else {
          subject = "Internal Memo: " + type.getName() + " review - Vol. " + i;
          summary =
              "Internal department circulation of "
                  + type.getDescription()
                  + " for faculty review, approval workflow, and record-keeping.";
        }

        DocumentModel doc =
            DocumentModel.builder()
                .direction(direction)
                .documentType(type)
                .senderOrganization(sender)
                .receiverOrganization(receiver)
                .documentNumber(docNum)
                .documentDate(docDate)
                .receivedDate(receivedDate)
                .subject(subject)
                .summary(summary)
                .confidentiality(confidentiality)
                .priority(priority)
                .status(status)
                .remarks("Auto-generated seed document #" + i + " for type " + type.getCode())
                .createdBy(adminOfficer)
                .build();

        doc = documentRepository.save(doc);

        // Save File for some documents (e.g., 50%)
        if (i % 2 == 0) {
          documentFileRepository.save(
              DocumentFileModel.builder()
                  .document(doc)
                  .uploadImage(dummyImage)
                  .fileName(type.getCode().toLowerCase() + "_" + i + "_official.pdf")
                  .filePath(
                      "/documents/2026/07/"
                          + type.getCode().toLowerCase()
                          + "_"
                          + i
                          + "_official.pdf")
                  .mimeType("application/pdf")
                  .fileSize(102400L * i)
                  .isPrimary(true)
                  .uploadedBy(adminOfficer)
                  .build());
        }

        // Save Logs for Document
        documentLogRepository.save(
            DocumentLogModel.builder()
                .document(doc)
                .officer(i % 2 == 0 ? adminOfficer : otherOfficer)
                .action("CREATE")
                .description("Document registered and seeded in the system.")
                .build());

        totalDocsSeeded++;
      }
    }

    System.out.println(
        "✅ Document Management seed data loaded successfully! Seeded "
            + totalDocsSeeded
            + " documents.");
  }
}
