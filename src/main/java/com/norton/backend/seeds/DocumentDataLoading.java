package com.norton.backend.seeds;

import com.norton.backend.models.DocumentTypeModel;
import com.norton.backend.repositories.DocumentTypeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(6)
// @Profile("dev")
public class DocumentDataLoading implements CommandLineRunner {

  private final DocumentTypeRepository documentTypeRepository;

  @Override
  public void run(String... args) {
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

    System.out.println("✅ Document Types seed data loaded successfully!");
  }
}
