package com.norton.backend.seeds;

import com.norton.backend.enums.DepartmentStatus;
import com.norton.backend.enums.GenderEnum;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.enums.PositionStatus;
import com.norton.backend.enums.UserStatus;
import com.norton.backend.models.DepartmentModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.PositionModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.DepartmentRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PositionRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@DependsOn("userDataLoading")
@RequiredArgsConstructor
@Order(4)
public class OfficerDataLoading implements CommandLineRunner {

  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final OfficerRepository officerRepository;
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    Map<String, DepartmentModel> departmentsByCode = loadOrCreateDepartments();
    Map<String, PositionModel> positionsByCode = loadOrCreatePositions(departmentsByCode);
    UserRoleModel officerRole = loadOfficerRole();

    List<OfficerSeed> officers = buildOfficerSeeds();
    for (OfficerSeed seed : officers) {
      if (officerRepository.findByOfficerCode(seed.officerCode()).isPresent()) {
        continue;
      }

      PositionModel position = positionsByCode.get(seed.positionCode().toUpperCase());
      if (position == null) {
        throw new RuntimeException("Position code not found: " + seed.positionCode());
      }

      UserModel user = loadOrCreateUser(seed, officerRole);
      if (user.getOfficer() != null) {
        user = createDedicatedUser(seed, officerRole);
      }
      OfficerModel officer = buildOfficer(seed, position, user);
      officerRepository.save(officer);
    }

    System.out.println("Officer Khmer seed data inserted/updated successfully.");
  }

  private Map<String, DepartmentModel> loadOrCreateDepartments() {
    Map<String, DepartmentModel> departmentsByCode = new HashMap<>();
    for (DepartmentModel department : departmentRepository.findAll()) {
      if (department.getCode() != null) {
        departmentsByCode.put(department.getCode().toUpperCase(), department);
      }
    }

    for (DepartmentSeed seed : buildDepartmentSeeds()) {
      String key = seed.code().toUpperCase();
      if (departmentsByCode.containsKey(key)) {
        continue;
      }

      DepartmentModel department =
          DepartmentModel.builder()
              .name(seed.name())
              .code(seed.code())
              .manager(seed.manager())
              .description(seed.description())
              .status(DepartmentStatus.ACTIVE)
              .build();
      department = departmentRepository.save(department);
      departmentsByCode.put(key, department);
    }
    return departmentsByCode;
  }

  private Map<String, PositionModel> loadOrCreatePositions(
      Map<String, DepartmentModel> departmentsByCode) {
    Map<String, PositionModel> positionsByCode = new HashMap<>();
    for (PositionModel position : positionRepository.findAll()) {
      if (position.getCode() != null) {
        positionsByCode.put(position.getCode().toUpperCase(), position);
      }
    }

    for (PositionSeed seed : buildPositionSeeds()) {
      String key = seed.code().toUpperCase();
      if (positionsByCode.containsKey(key)) {
        continue;
      }

      DepartmentModel department = departmentsByCode.get(seed.departmentCode().toUpperCase());
      if (department == null) {
        throw new RuntimeException("Department code not found: " + seed.departmentCode());
      }

      PositionModel position =
          PositionModel.builder()
              .name(seed.name())
              .code(seed.code())
              .department(department)
              .description(seed.description())
              .status(PositionStatus.ACTIVE)
              .build();
      position = positionRepository.save(position);
      positionsByCode.put(key, position);
    }
    return positionsByCode;
  }

  private UserRoleModel loadOfficerRole() {
    return userRoleRepository
        .findByRoleName("ROLE_OFFICER")
        .orElseThrow(() -> new RuntimeException("ROLE_OFFICER not found"));
  }

  private UserModel loadOrCreateUser(OfficerSeed seed, UserRoleModel officerRole) {
    return userRepository
        .findByUsername(seed.username())
        .orElseGet(
            () ->
                userRepository.save(
                    UserModel.builder()
                        .uuid(UUID.randomUUID())
                        .username(seed.username())
                        .email(seed.userEmail())
                        .fullName(seed.firstName() + " " + seed.lastName())
                        .passwordHash(passwordEncoder.encode("officer123"))
                        .role(officerRole)
                        .userStatus(UserStatus.ACTIVE)
                        .build()));
  }

  private UserModel createDedicatedUser(OfficerSeed seed, UserRoleModel officerRole) {
    String baseUsername = "seed_" + seed.officerCode().toLowerCase();
    String username = baseUsername;
    int sequence = 1;
    while (userRepository.existsByUsername(username)) {
      username = baseUsername + "_" + sequence;
      sequence++;
    }

    String email = username + "@dummy.com";
    return userRepository.save(
        UserModel.builder()
            .uuid(UUID.randomUUID())
            .username(username)
            .email(email)
            .fullName(seed.firstName() + " " + seed.lastName())
            .passwordHash(passwordEncoder.encode("officer123"))
            .role(officerRole)
            .userStatus(UserStatus.ACTIVE)
            .build());
  }

  private OfficerModel buildOfficer(OfficerSeed seed, PositionModel position, UserModel user) {
    return OfficerModel.builder()
        .uuid(UUID.randomUUID().toString())
        .officerCode(seed.officerCode())
        .firstName(seed.firstName())
        .lastName(seed.lastName())
        .gender(seed.gender())
        .phone(seed.phone())
        .email(seed.officerEmail())
        .position(position)
        .status(OfficerStatus.ACTIVE)
        .user(user)
        .build();
  }

  private List<DepartmentSeed> buildDepartmentSeeds() {
    return List.of(
        new DepartmentSeed(
            "DEP-01", "នាយកដ្ឋានរដ្ឋបាល", "លោក សុខ វុទ្ធី", "គ្រប់គ្រងការងាររដ្ឋបាលទូទៅ"),
        new DepartmentSeed(
            "DEP-02",
            "នាយកដ្ឋានធនធានមនុស្ស",
            "លោកស្រី ចាន់ ស្រីពៅ",
            "គ្រប់គ្រងបុគ្គលិក និងវិន័យការងារ"),
        new DepartmentSeed(
            "DEP-03", "នាយកដ្ឋានហិរញ្ញវត្ថុ", "លោក ម៉ៅ រតនា", "គ្រប់គ្រងថវិកា និងចំណាយ"),
        new DepartmentSeed(
            "DEP-04", "នាយកដ្ឋានព័ត៌មានវិទ្យា", "លោក លី ដារ៉ា", "ថែទាំប្រព័ន្ធបច្ចេកវិទ្យាព័ត៌មាន"),
        new DepartmentSeed("DEP-05", "នាយកដ្ឋានផែនការ", "លោក ឈុន សុភ័ក្រ", "រៀបចំផែនការអភិវឌ្ឍន៍"),
        new DepartmentSeed(
            "DEP-06", "នាយកដ្ឋានលទ្ធកម្ម", "លោក តាំង វិសាល", "គ្រប់គ្រងការទិញ និងផ្គត់ផ្គង់"),
        new DepartmentSeed(
            "DEP-07", "នាយកដ្ឋានច្បាប់", "លោកស្រី ហេង មល្លិកា", "ផ្តល់យោបល់ផ្នែកច្បាប់"),
        new DepartmentSeed(
            "DEP-08", "នាយកដ្ឋានបណ្ណសារ", "លោក រ៉ា វណ្ណៈ", "គ្រប់គ្រងឯកសារ និងបណ្ណសារ"),
        new DepartmentSeed(
            "DEP-09",
            "នាយកដ្ឋានទំនាក់ទំនងសាធារណៈ",
            "លោកស្រី សៀង លក្ខិណា",
            "ផ្សព្វផ្សាយព័ត៌មានស្ថាប័ន"),
        new DepartmentSeed(
            "DEP-10",
            "នាយកដ្ឋានសវនកម្មផ្ទៃក្នុង",
            "លោក គឹម សុវណ្ណ",
            "ត្រួតពិនិត្យប្រព័ន្ធគ្រប់គ្រងផ្ទៃក្នុង"));
  }

  private List<PositionSeed> buildPositionSeeds() {
    return List.of(
        new PositionSeed("POS-01", "ប្រធាននាយកដ្ឋាន", "DEP-01", "ដឹកនាំនាយកដ្ឋាន"),
        new PositionSeed("POS-02", "អនុប្រធាននាយកដ្ឋាន", "DEP-02", "ជួយសម្របសម្រួលការងារនាយកដ្ឋាន"),
        new PositionSeed("POS-03", "មន្រ្តីរដ្ឋបាល", "DEP-03", "អនុវត្តការងាររដ្ឋបាលប្រចាំថ្ងៃ"),
        new PositionSeed("POS-04", "មន្រ្តីធនធានមនុស្ស", "DEP-04", "គ្រប់គ្រងទិន្នន័យបុគ្គលិក"),
        new PositionSeed("POS-05", "គណនេយ្យករ", "DEP-05", "កត់ត្រា និងត្រួតពិនិត្យហិរញ្ញវត្ថុ"),
        new PositionSeed("POS-06", "អ្នកអភិវឌ្ឍន៍ប្រព័ន្ធ", "DEP-06", "អភិវឌ្ឍន៍ប្រព័ន្ធព័ត៌មាន"),
        new PositionSeed(
            "POS-07", "អ្នកវិភាគទិន្នន័យ", "DEP-07", "វិភាគទិន្នន័យសម្រាប់សេចក្ដីសម្រេច"),
        new PositionSeed("POS-08", "មន្រ្តីផែនការ", "DEP-08", "រៀបចំ និងតាមដានផែនការ"),
        new PositionSeed("POS-09", "មន្រ្តីទំនាក់ទំនង", "DEP-09", "ទំនាក់ទំនងជាមួយសាធារណៈ"),
        new PositionSeed("POS-10", "មន្រ្តីឯកសារ", "DEP-10", "គ្រប់គ្រងឯកសារផ្លូវការ"));
  }

  private List<OfficerSeed> buildOfficerSeeds() {
    List<OfficerSeed> seeds = new ArrayList<>();
    seeds.add(
        new OfficerSeed(
            "OFF-001",
            "admin",
            "admin.officer@dummy.com",
            "officer001@dummy.com",
            "សុខ",
            "វិរៈ",
            GenderEnum.MALE,
            "010000001",
            "POS-01"));
    seeds.add(
        new OfficerSeed(
            "OFF-002",
            "user",
            "user.officer@dummy.com",
            "officer002@dummy.com",
            "ចាន់",
            "ស្រីនិត",
            GenderEnum.FEMALE,
            "010000002",
            "POS-02"));
    seeds.add(
        new OfficerSeed(
            "OFF-003",
            "Kelly",
            "kelly.officer@dummy.com",
            "officer003@dummy.com",
            "ឈិន",
            "គីលី",
            GenderEnum.MALE,
            "010000003",
            "POS-03"));
    seeds.add(
        new OfficerSeed(
            "OFF-004",
            "vannchhai",
            "vannchhai.officer@dummy.com",
            "officer004@dummy.com",
            "វណ្ណឆៃ",
            "ចាន់",
            GenderEnum.MALE,
            "010000004",
            "POS-04"));
    seeds.add(
        new OfficerSeed(
            "OFF-005",
            "officer005",
            "officer005@dummy.com",
            "officer005@dummy.com",
            "ស្រីពៅ",
            "លីណា",
            GenderEnum.FEMALE,
            "010000005",
            "POS-05"));
    seeds.add(
        new OfficerSeed(
            "OFF-006",
            "officer006",
            "officer006@dummy.com",
            "officer006@dummy.com",
            "ដារ៉ា",
            "សុភក្ត្រ",
            GenderEnum.MALE,
            "010000006",
            "POS-06"));
    seeds.add(
        new OfficerSeed(
            "OFF-007",
            "officer007",
            "officer007@dummy.com",
            "officer007@dummy.com",
            "មាលា",
            "រីណា",
            GenderEnum.FEMALE,
            "010000007",
            "POS-07"));
    seeds.add(
        new OfficerSeed(
            "OFF-008",
            "officer008",
            "officer008@dummy.com",
            "officer008@dummy.com",
            "សុវណ្ណ",
            "មុនី",
            GenderEnum.MALE,
            "010000008",
            "POS-08"));
    seeds.add(
        new OfficerSeed(
            "OFF-009",
            "officer009",
            "officer009@dummy.com",
            "officer009@dummy.com",
            "គន្ធា",
            "ចរិយា",
            GenderEnum.FEMALE,
            "010000009",
            "POS-09"));
    seeds.add(
        new OfficerSeed(
            "OFF-010",
            "officer010",
            "officer010@dummy.com",
            "officer010@dummy.com",
            "ប៊ុនថន",
            "ពេជ្រ",
            GenderEnum.MALE,
            "010000010",
            "POS-10"));
    seeds.add(
        new OfficerSeed(
            "OFF-011",
            "officer011",
            "officer011@dummy.com",
            "officer011@dummy.com",
            "សុភា",
            "វណ្ណា",
            GenderEnum.FEMALE,
            "010000011",
            "POS-01"));
    seeds.add(
        new OfficerSeed(
            "OFF-012",
            "officer012",
            "officer012@dummy.com",
            "officer012@dummy.com",
            "រ័ត្ន",
            "វិសាល",
            GenderEnum.MALE,
            "010000012",
            "POS-02"));
    seeds.add(
        new OfficerSeed(
            "OFF-013",
            "officer013",
            "officer013@dummy.com",
            "officer013@dummy.com",
            "ស្រីល័ក្ខ",
            "នារី",
            GenderEnum.FEMALE,
            "010000013",
            "POS-03"));
    seeds.add(
        new OfficerSeed(
            "OFF-014",
            "officer014",
            "officer014@dummy.com",
            "officer014@dummy.com",
            "ពិសី",
            "កក្កដា",
            GenderEnum.MALE,
            "010000014",
            "POS-04"));
    seeds.add(
        new OfficerSeed(
            "OFF-015",
            "officer015",
            "officer015@dummy.com",
            "officer015@dummy.com",
            "មុនីរ័ត្ន",
            "សុធា",
            GenderEnum.FEMALE,
            "010000015",
            "POS-05"));
    seeds.add(
        new OfficerSeed(
            "OFF-016",
            "officer016",
            "officer016@dummy.com",
            "officer016@dummy.com",
            "សុភ័ក្រ",
            "រតនា",
            GenderEnum.MALE,
            "010000016",
            "POS-06"));
    seeds.add(
        new OfficerSeed(
            "OFF-017",
            "officer017",
            "officer017@dummy.com",
            "officer017@dummy.com",
            "ច័ន្ទរត្ន",
            "ស្រីមុំ",
            GenderEnum.FEMALE,
            "010000017",
            "POS-07"));
    seeds.add(
        new OfficerSeed(
            "OFF-018",
            "officer018",
            "officer018@dummy.com",
            "officer018@dummy.com",
            "វិជ្ជា",
            "ថាវី",
            GenderEnum.MALE,
            "010000018",
            "POS-08"));
    seeds.add(
        new OfficerSeed(
            "OFF-019",
            "officer019",
            "officer019@dummy.com",
            "officer019@dummy.com",
            "ស្រីនាង",
            "ធីតា",
            GenderEnum.FEMALE,
            "010000019",
            "POS-09"));
    seeds.add(
        new OfficerSeed(
            "OFF-020",
            "officer020",
            "officer020@dummy.com",
            "officer020@dummy.com",
            "ជ័យ",
            "សុផល",
            GenderEnum.MALE,
            "010000020",
            "POS-10"));
    return seeds;
  }

  private record DepartmentSeed(String code, String name, String manager, String description) {}

  private record PositionSeed(
      String code, String name, String departmentCode, String description) {}

  private record OfficerSeed(
      String officerCode,
      String username,
      String userEmail,
      String officerEmail,
      String firstName,
      String lastName,
      GenderEnum gender,
      String phone,
      String positionCode) {}
}
