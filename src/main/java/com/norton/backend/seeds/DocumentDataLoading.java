package com.norton.backend.seeds;

import com.norton.backend.models.DocumentModel;
import com.norton.backend.models.DocumentTypeModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.OrganizationModel;
import com.norton.backend.repositories.DocumentRepository;
import com.norton.backend.repositories.DocumentTypeRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.OrganizationRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Order(6)
// @Profile("dev")
public class DocumentDataLoading implements CommandLineRunner {

  private final DocumentTypeRepository documentTypeRepository;
  private final DocumentRepository documentRepository;
  private final OfficerRepository officerRepository;
  private final OrganizationRepository organizationRepository;

  @Override
  public void run(String... args) {
    seedDocumentTypes();
    seedOrganizations();
    seedMockDocuments();
  }

  private void seedDocumentTypes() {
    record DocTypeData(String name, String code, String description) {}

    List<DocTypeData> docTypes =
        List.of(
            new DocTypeData("លិខិតស្នើ", "REQ_LTR", "លិខិតស្នើ (Request Letter)"),
            new DocTypeData("សេចក្តីជូនដំណឹង", "NOTICE", "សេចក្តីជូនដំណឹង (Notice / Announcement)"),
            new DocTypeData("លិខិតអញ្ជើញ", "INV_LTR", "លិខិតអញ្ជើញ (Invitation Letter)"),
            new DocTypeData("លិខិតបញ្ជាបេសកកម្ម", "MSN_ORD", "លិខិតបញ្ជាបេសកកម្ម (Mission Order)"),
            new DocTypeData(
                "លិខិតចុះទិដ្ឋាការធ្វើដំណើរ",
                "TRV_VISA",
                "លិខិតចុះទិដ្ឋាការធ្វើដំណើរ (Travel Visa/Permit)"),
            new DocTypeData(
                "លិខិតផ្ទេរសិទ្ធិ", "DEL_AUTH", "លិខិតផ្ទេរសិទ្ធិ (Delegation of Authority)"),
            new DocTypeData(
                "លិខិតប្រគល់សិទ្ធិចុះហត្ថលេខា",
                "POA_SIGN",
                "លិខិតប្រគល់សិទ្ធិចុះហត្ថលេខា (Power of Attorney to Sign)"),
            new DocTypeData("ដីកាអម", "COV_LTR", "ដីកាអម (Covering Letter)"),
            new DocTypeData(
                "លិខិតអនុញ្ញាតច្បាប់ឈប់សម្រាក",
                "LV_PERM",
                "លិខិតអនុញ្ញាតច្បាប់ឈប់សម្រាក (Leave Permission)"),
            new DocTypeData("កំណត់បង្ហាញរឿង", "CASE_MEMO", "កំណត់បង្ហាញរឿង (Case Presentation)"),
            new DocTypeData("របាយការណ៍", "REPORT", "របាយការណ៍ (Report)"),
            new DocTypeData("កំណត់ហេតុ", "MINUTES", "កំណត់ហេតុ (Minutes / Record)"),
            new DocTypeData("លិខិតបញ្ជាក់", "CERT_LTR", "លិខិតបញ្ជាក់ (Certification Letter)"));

    for (DocTypeData data : docTypes) {
      documentTypeRepository
          .findByCode(data.code())
          .ifPresentOrElse(
              existing -> {
                existing.setName(data.name());
                existing.setDescription(data.description());
                documentTypeRepository.save(existing);
              },
              () -> {
                documentTypeRepository.save(
                    DocumentTypeModel.builder()
                        .name(data.name())
                        .code(data.code())
                        .description(data.description())
                        .build());
              });
    }

    log.info("✅ Document Types seed data loaded successfully!");
  }

  private void seedOrganizations() {
    record OrgData(
        String name,
        String shortName,
        String type,
        String phone,
        String email,
        String address,
        String website) {}

    List<OrgData> orgs =
        List.of(
            new OrgData(
                "សាកលវិទ្យាល័យ ន័រតុន",
                "NU",
                "University",
                "023 432 070",
                "info@norton-u.com",
                "St. Keo Chenda, Chroy Changvar, Phnom Penh",
                "https://www.norton-u.com"),
            new OrgData(
                "ក្រសួងអប់រំ យុវជន និងកីឡា",
                "MoEYS",
                "Ministry",
                "023 210 140",
                "info@moeys.gov.kh",
                "No. 80, Preah Norodom Blvd, Phnom Penh",
                "http://www.moeys.gov.kh"),
            new OrgData(
                "ក្រសួងប្រៃសណីយ៍ និងទូរគមនាគមន៍",
                "MPTC",
                "Ministry",
                "023 724 811",
                "info@mptc.gov.kh",
                "Building 13, Monivong Blvd, Phnom Penh",
                "https://mptc.gov.kh"),
            new OrgData(
                "ក្រសួងសេដ្ឋកិច្ច និងហិរញ្ញវត្ថុ",
                "MEF",
                "Ministry",
                "023 890 666",
                "admin@mef.gov.kh",
                "St. 92, Sangkat Wat Phnom, Phnom Penh",
                "https://mef.gov.kh"),
            new OrgData(
                "សាលារាជធានីភ្នំពេញ",
                "PPCH",
                "Municipality",
                "023 722 054",
                "phnompenh@interior.gov.kh",
                "Preah Monivong Blvd, Phnom Penh",
                "http://phnompenh.gov.kh"),
            new OrgData(
                "នាយកដ្ឋានឧត្តមសិក្សា",
                "DHE",
                "Department",
                "023 217 733",
                "dhe@moeys.gov.kh",
                "Preah Norodom Blvd, Phnom Penh",
                "http://www.dhe.moeys.gov.kh"));

    for (OrgData data : orgs) {
      if (organizationRepository.findByName(data.name()).isEmpty()) {
        organizationRepository.save(
            OrganizationModel.builder()
                .name(data.name())
                .shortName(data.shortName())
                .organizationType(data.type())
                .phone(data.phone())
                .email(data.email())
                .address(data.address())
                .website(data.website())
                .status("ACTIVE")
                .build());
      }
    }

    log.info("✅ Organizations seed data checked/loaded successfully!");
  }

  private void seedMockDocuments() {
    if (documentRepository.count() >= 15) {
      log.info("Documents database already populated with at least 15 records.");
      return;
    }

    List<OfficerModel> officers = officerRepository.findAll();
    if (officers.isEmpty()) {
      log.warn("No officers found. Skipping mock documents seeding.");
      return;
    }

    OfficerModel officer1 = officers.get(0);
    OfficerModel officer2 = officers.size() > 1 ? officers.get(1) : officer1;
    OfficerModel officer3 = officers.size() > 2 ? officers.get(2) : officer1;
    OfficerModel officer4 = officers.size() > 3 ? officers.get(3) : officer1;

    Map<String, DocumentTypeModel> docTypeMap = new HashMap<>();
    documentTypeRepository.findAll().forEach(dt -> docTypeMap.put(dt.getCode(), dt));

    Map<String, OrganizationModel> orgMap = new HashMap<>();
    organizationRepository.findAll().forEach(org -> orgMap.put(org.getShortName(), org));

    OrganizationModel nu = orgMap.get("NU");
    OrganizationModel moeys = orgMap.get("MoEYS");
    OrganizationModel mptc = orgMap.get("MPTC");
    OrganizationModel mef = orgMap.get("MEF");
    OrganizationModel ppch = orgMap.get("PPCH");
    OrganizationModel dhe = orgMap.get("DHE");

    List<DocumentModel> mockDocs =
        List.of(
            // 1. INCOMING - Request Letter
            DocumentModel.builder()
                .direction("INCOMING")
                .documentType(docTypeMap.get("REQ_LTR"))
                .senderOrganization(moeys)
                .receiverOrganization(nu)
                .documentNumber("០១២/២០២៦ ស.អ.យ")
                .documentDate(LocalDate.now().minusDays(45))
                .receivedDate(LocalDate.now().minusDays(44))
                .subject(
                    "លិខិតស្នើសុំបញ្ជីឈ្មោះនិស្សិតអាហារូបករណ៍ឆ្នើមសម្រាប់ឆ្នាំសិក្សា ២០២៥-២០២៦")
                .summary(
                    "ក្រសួងស្នើសុំសាកលវិទ្យាល័យផ្តល់ទិន្នន័យ និងបញ្ជីឈ្មោះនិស្សិតអាហារូបករណ៍ឆ្នើមដើម្បីរៀបចំពិធីប្រគល់រង្វាន់លើកទឹកចិត្ត។")
                .confidentiality("NORMAL")
                .priority("HIGH")
                .status("COMPLETED")
                .remarks("បានបញ្ជូនទៅការិយាល័យសិក្សារៀបចំរួចរាល់")
                .createdBy(officer1)
                .build(),

            // 2. INCOMING - Invitation Letter
            DocumentModel.builder()
                .direction("INCOMING")
                .documentType(docTypeMap.get("INV_LTR"))
                .senderOrganization(mptc)
                .receiverOrganization(nu)
                .documentNumber("០២៤/២០២៦ ក.ប.ទ")
                .documentDate(LocalDate.now().minusDays(40))
                .receivedDate(LocalDate.now().minusDays(39))
                .subject(
                    "លិខិតអញ្ជើញចូលរួមសិក្ខាសាលាថ្នាក់ជាតិស្តីពីបរិវត្តកម្មឌីជីថលក្នុងវិស័យអប់រំជាន់ខ្ពស់")
                .summary(
                    "អញ្ជើញថ្នាក់ដឹកនាំ និងសាស្ត្រាចារ្យផ្នែកបច្ចេកវិទ្យាព័ត៌មានចូលរួមសិក្ខាសាលារយៈពេល ២ ថ្ងៃ នៅទីស្តីការក្រសួង។")
                .confidentiality("NORMAL")
                .priority("NORMAL")
                .status("APPROVED")
                .remarks("ចាត់តាំងតំណាងមហាវិទ្យាល័យវិទ្យាសាស្ត្រកុំព្យូទ័រចូលរួម")
                .createdBy(officer2)
                .build(),

            // 3. INCOMING - Notice
            DocumentModel.builder()
                .direction("INCOMING")
                .documentType(docTypeMap.get("NOTICE"))
                .senderOrganization(ppch)
                .receiverOrganization(nu)
                .documentNumber("០៦៧/២០២៦ ស.រ.ភ")
                .documentDate(LocalDate.now().minusDays(35))
                .receivedDate(LocalDate.now().minusDays(34))
                .subject(
                    "សេចក្តីជូនដំណឹងស្តីពីការរៀបចំសណ្តាប់ធ្នាប់សាធារណៈ និងចរាចរណ៍ជុំវិញតំបន់ជ្រោយចង្វារ")
                .summary(
                    "ណែនាំអំពីវិធានការរក្សាសណ្តាប់ធ្នាប់ចំណតយានយន្ត និងសុវត្ថិភាពចរាចរណ៍ក្នុងតំបន់សាកលវិទ្យាល័យ។")
                .confidentiality("NORMAL")
                .priority("URGENT")
                .status("COMPLETED")
                .remarks("បានជូនដំណឹងដល់ក្រុមសន្តិសុខ និងបុគ្គលិកគ្រប់ផ្នែក")
                .createdBy(officer3)
                .build(),

            // 4. INCOMING - Covering Letter
            DocumentModel.builder()
                .direction("INCOMING")
                .documentType(docTypeMap.get("COV_LTR"))
                .senderOrganization(dhe)
                .receiverOrganization(nu)
                .documentNumber("១០២/២០២៦ ក.អ.យ")
                .documentDate(LocalDate.now().minusDays(30))
                .receivedDate(LocalDate.now().minusDays(29))
                .subject(
                    "ដីកាអមភ្ជាប់មកជាមួយនូវគោលការណ៍ណែនាំថ្មីស្តីពីការវាយតម្លៃគុណភាពអប់រំផ្ទៃក្នុង")
                .summary(
                    "ផ្ញើឯកសារគោលការណ៍ណែនាំស្តង់ដារគុណភាពអប់រំសម្រាប់អនុវត្តតាមគ្រឹះស្ថានឧត្តមសិក្សា។")
                .confidentiality("CONFIDENTIAL")
                .priority("NORMAL")
                .status("IN_PROGRESS")
                .remarks("កំពុងត្រួតពិនិត្យដោយគណៈកម្មការធានាគុណភាពអប់រំ")
                .createdBy(officer1)
                .build(),

            // 5. INCOMING - Request Letter
            DocumentModel.builder()
                .direction("INCOMING")
                .documentType(docTypeMap.get("REQ_LTR"))
                .senderOrganization(mef)
                .receiverOrganization(nu)
                .documentNumber("១៤៥/២០២៦ ក.ស.ហ")
                .documentDate(LocalDate.now().minusDays(25))
                .receivedDate(LocalDate.now().minusDays(24))
                .subject("លិខិតស្នើសុំកិច្ចសហការរៀបចំកម្មវិធីបណ្តុះបណ្តាលជំនាញហិរញ្ញវត្ថុសាធារណៈ")
                .summary(
                    "ស្នើសុំប្រើប្រាស់សាលប្រជុំ និងកិច្ចសហការបច្ចេកទេសសម្រាប់វគ្គបណ្តុះបណ្តាលមន្ត្រីរាជការ។")
                .confidentiality("NORMAL")
                .priority("NORMAL")
                .status("APPROVED")
                .remarks("បានឯកភាពកាលបរិច្ឆេទ និងទីកន្លែង")
                .createdBy(officer4)
                .build(),

            // 6. OUTGOING - Invitation Letter
            DocumentModel.builder()
                .direction("OUTGOING")
                .documentType(docTypeMap.get("INV_LTR"))
                .senderOrganization(nu)
                .receiverOrganization(moeys)
                .documentNumber("០១៥/២០២៦ ស.ន.ត")
                .documentDate(LocalDate.now().minusDays(22))
                .receivedDate(LocalDate.now().minusDays(21))
                .subject(
                    "លិខិតអញ្ជើញជាអធិបតីក្នុងពិធីសម្ពោធមជ្ឈមណ្ឌលស្រាវជ្រាវបច្ចេកវិទ្យា AI និងនវានុវត្តន៍")
                .summary(
                    "គោរពអញ្ជើញឯកឧត្តមរដ្ឋមន្ត្រីក្រសួងអប់រំចូលរួមជាអធិបតីដ៏ខ្ពង់ខ្ពស់ក្នុងពិធីសម្ពោធមជ្ឈមណ្ឌលថ្មី។")
                .confidentiality("NORMAL")
                .priority("HIGH")
                .status("APPROVED")
                .remarks("រង់ចាំការបញ្ជាក់វត្តមានផ្លូវការ")
                .createdBy(officer1)
                .build(),

            // 7. OUTGOING - Report
            DocumentModel.builder()
                .direction("OUTGOING")
                .documentType(docTypeMap.get("REPORT"))
                .senderOrganization(nu)
                .receiverOrganization(dhe)
                .documentNumber("០៣១/២០២៦ ស.ន.ត/រដ")
                .documentDate(LocalDate.now().minusDays(18))
                .receivedDate(LocalDate.now().minusDays(17))
                .subject(
                    "របាយការណ៍វឌ្ឍនភាពនៃការអនុវត្តកម្មវិធីសិក្សា និងចំនួននិស្សិតចុះឈ្មោះឆមាសទី១")
                .summary(
                    "បញ្ជូនរបាយការណ៍ស្ថិតិ និងវឌ្ឍនភាពសិក្សាប្រចាំឆមាសជូននាយកដ្ឋានឧត្តមសិក្សា។")
                .confidentiality("NORMAL")
                .priority("NORMAL")
                .status("COMPLETED")
                .remarks("បានបញ្ជូនឯកសារច្បាប់ដើមរួចរាល់")
                .createdBy(officer2)
                .build(),

            // 8. OUTGOING - Request Letter
            DocumentModel.builder()
                .direction("OUTGOING")
                .documentType(docTypeMap.get("REQ_LTR"))
                .senderOrganization(nu)
                .receiverOrganization(ppch)
                .documentNumber("០៥២/២០២៦ ស.ន.ត/អ.ប")
                .documentDate(LocalDate.now().minusDays(15))
                .receivedDate(LocalDate.now().minusDays(14))
                .subject(
                    "លិខិតស្នើសុំកិច្ចសហការសម្រួលចរាចរណ៍ក្នុងពិធីប្រគល់សញ្ញាបត្រប្រចាំឆ្នាំ ២០២៦")
                .summary(
                    "ស្នើសុំកម្លាំងសមត្ថកិច្ចជួយសម្រួលចរាចរណ៍ជុំវិញបរិវេណសាកលវិទ្យាល័យសម្រាប់ពិធីចែកសញ្ញាបត្រ។")
                .confidentiality("NORMAL")
                .priority("URGENT")
                .status("APPROVED")
                .remarks("បានទទួលការឯកភាពពីសាលារាជធានី")
                .createdBy(officer3)
                .build(),

            // 9. OUTGOING - Certification Letter
            DocumentModel.builder()
                .direction("OUTGOING")
                .documentType(docTypeMap.get("CERT_LTR"))
                .senderOrganization(nu)
                .receiverOrganization(mptc)
                .documentNumber("០៧១/២០២៦ ស.ន.ត")
                .documentDate(LocalDate.now().minusDays(12))
                .receivedDate(LocalDate.now().minusDays(11))
                .subject("លិខិតបញ្ជាក់សមត្ថភាព និងការចូលរួមគម្រោងអភិវឌ្ឍន៍ប្រព័ន្ធឌីជីថលថ្នាក់ជាតិ")
                .summary(
                    "បញ្ជាក់ការចូលរួមរបស់ក្រុមការងារស្រាវជ្រាវសាកលវិទ្យាល័យក្នុងគម្រោងបង្កើតថ្នាលបច្ចេកវិទ្យា។")
                .confidentiality("NORMAL")
                .priority("NORMAL")
                .status("COMPLETED")
                .remarks("ប្រគល់ជូនតំណាងក្រសួងផ្ទាល់")
                .createdBy(officer1)
                .build(),

            // 10. OUTGOING - Delegation of Authority
            DocumentModel.builder()
                .direction("OUTGOING")
                .documentType(docTypeMap.get("DEL_AUTH"))
                .senderOrganization(nu)
                .receiverOrganization(moeys)
                .documentNumber("០៨៣/២០២៦ ស.ន.ត/ធ.ម")
                .documentDate(LocalDate.now().minusDays(10))
                .receivedDate(LocalDate.now().minusDays(9))
                .subject(
                    "លិខិតផ្ទេរសិទ្ធិតំណាងសាកលវិទ្យាធិការក្នុងការចូលរួមកិច្ចប្រជុំក្រុមប្រឹក្សាភិបាលអប់រំ")
                .summary(
                    "ផ្ទេរសិទ្ធិជូនសាកលវិទ្យាធិការរងជាតំណាងពេញសិទ្ធិក្នុងការចូលរួមកិច្ចប្រជុំ និងសម្រេចចិត្ត។")
                .confidentiality("CONFIDENTIAL")
                .priority("URGENT")
                .status("APPROVED")
                .remarks("ឯកសារមានសុពលភាពរយៈពេល ៣ ថ្ងៃ")
                .createdBy(officer4)
                .build(),

            // 11. INTERNAL - Notice
            DocumentModel.builder()
                .direction("INTERNAL")
                .documentType(docTypeMap.get("NOTICE"))
                .senderOrganization(nu)
                .receiverOrganization(nu)
                .documentNumber("០៩៥/២០២៦ ស.ន.ត/គ.ជ")
                .documentDate(LocalDate.now().minusDays(8))
                .receivedDate(LocalDate.now().minusDays(8))
                .subject("សេចក្តីជូនដំណឹងស្តីពីកាលវិភាគប្រឡងបញ្ចប់ឆមាសទី១ ឆ្នាំសិក្សា ២០២៥-២០២៦")
                .summary(
                    "ជូនដំណឹងដល់មហាវិទ្យាល័យ និងដេប៉ាតឺម៉ង់ទាំងអស់អំពីការរៀបចំកាលវិភាគ និងវិធានការប្រឡង។")
                .confidentiality("NORMAL")
                .priority("URGENT")
                .status("APPROVED")
                .remarks("ផ្សព្វផ្សាយលើក្តារព័ត៌មាន និងប្រព័ន្ធគ្រប់គ្រងនិស្សិត")
                .createdBy(officer2)
                .build(),

            // 12. INTERNAL - Mission Order
            DocumentModel.builder()
                .direction("INTERNAL")
                .documentType(docTypeMap.get("MSN_ORD"))
                .senderOrganization(nu)
                .receiverOrganization(nu)
                .documentNumber("១១៥/២០២៦ ស.ន.ត/រ.ប")
                .documentDate(LocalDate.now().minusDays(6))
                .receivedDate(LocalDate.now().minusDays(6))
                .subject(
                    "លិខិតបញ្ជាបេសកកម្មចុះផ្សព្វផ្សាយកម្មវិធីអាហារូបករណ៍នៅតាមបណ្តាខេត្តភូមិភាគឦសាន")
                .summary(
                    "ចាត់តាំងក្រុមការងារចំនួន ៥ រូប ចុះផ្សព្វផ្សាយការអប់រំ និងណែនាំជំនាញសិក្សាដល់សិស្សវិទ្យាល័យ។")
                .confidentiality("NORMAL")
                .priority("NORMAL")
                .status("APPROVED")
                .remarks("រយៈពេលបេសកកម្ម ៥ ថ្ងៃ (ខេត្តក្រចេះ ស្ទឹងត្រែង និងរតនគិរី)")
                .createdBy(officer3)
                .build(),

            // 13. INTERNAL - Minutes
            DocumentModel.builder()
                .direction("INTERNAL")
                .documentType(docTypeMap.get("MINUTES"))
                .senderOrganization(nu)
                .receiverOrganization(nu)
                .documentNumber("១២៣/២០២៦ ស.ន.ត/ក.ហ")
                .documentDate(LocalDate.now().minusDays(4))
                .receivedDate(LocalDate.now().minusDays(4))
                .subject("កំណត់ហេតុអង្គប្រជុំគណៈគ្រប់គ្រងសាកលវិទ្យាល័យប្រចាំខែកុម្ភៈ ឆ្នាំ២០២៦")
                .summary(
                    "កត់ត្រាមតិពិភាក្សា ការវាយតម្លៃការងារប្រចាំខែ និងផែនការសកម្មភាពសម្រាប់ខែបន្ទាប់។")
                .confidentiality("CONFIDENTIAL")
                .priority("NORMAL")
                .status("COMPLETED")
                .remarks("បានចែករំលែកជូនសមាជិកអង្គប្រជុំរួចរាល់")
                .createdBy(officer1)
                .build(),

            // 14. INTERNAL - Case Memo
            DocumentModel.builder()
                .direction("INTERNAL")
                .documentType(docTypeMap.get("CASE_MEMO"))
                .senderOrganization(nu)
                .receiverOrganization(nu)
                .documentNumber("១៣៤/២០២៦ ស.ន.ត/ល.ប")
                .documentDate(LocalDate.now().minusDays(2))
                .receivedDate(LocalDate.now().minusDays(2))
                .subject(
                    "កំណត់បង្ហាញរឿងស្តីពីសំណើសុំកែលម្អប្រព័ន្ធបណ្តាញអ៊ីនធឺណិត និងបន្ទប់ពិសោធន៍កុំព្យូទ័រ")
                .summary(
                    "បង្ហាញពីស្ថានភាពជាក់ស្តែង តម្រូវការបច្ចេកទេស និងការប៉ាន់ប្រមាណថវិកាសម្រាប់ដំឡើងឧបករណ៍ថ្មី។")
                .confidentiality("NORMAL")
                .priority("HIGH")
                .status("IN_PROGRESS")
                .remarks("កំពុងរង់ចាំការពិនិត្យពីផ្នែកហិរញ្ញវត្ថុ")
                .createdBy(officer4)
                .build(),

            // 15. INTERNAL - Power of Attorney to Sign
            DocumentModel.builder()
                .direction("INTERNAL")
                .documentType(docTypeMap.get("POA_SIGN"))
                .senderOrganization(nu)
                .receiverOrganization(nu)
                .documentNumber("១៤២/២០២៦ ស.ន.ត/ប.ម")
                .documentDate(LocalDate.now().minusDays(1))
                .receivedDate(LocalDate.now().minusDays(1))
                .subject(
                    "លិខិតប្រគល់សិទ្ធិចុះហត្ថលេខាលើលិខិតបញ្ជាក់ការសិក្សា និងព្រឹត្តិបត្រពិន្ទុបណ្តោះអាសន្ន")
                .summary(
                    "ប្រគល់សិទ្ធិជូនប្រធានការិយាល័យសិក្សាដើម្បីចុះហត្ថលេខាលើឯកសារសិក្សាក្នុងអំឡុងពេលសាកលវិទ្យាធិការបំពេញបេសកកម្មក្រៅប្រទេស។")
                .confidentiality("CONFIDENTIAL")
                .priority("HIGH")
                .status("APPROVED")
                .remarks("មានប្រសិទ្ធភាពចាប់ពីថ្ងៃទី ១៥ ដល់ ២៥ ខែមីនា ឆ្នាំ២០២៦")
                .createdBy(officer1)
                .build());

    for (DocumentModel doc : mockDocs) {
      if (doc.getDocumentType() != null
          && doc.getDocumentNumber() != null
          && documentRepository
              .findByDocumentNumberAndDocumentTypeId(
                  doc.getDocumentNumber(), doc.getDocumentType().getId())
              .isEmpty()) {
        documentRepository.save(doc);
      }
    }

    log.info("✅ 15 Mock Documents seed data loaded successfully!");
  }
}
