package com.norton.backend.seeds;

import com.norton.backend.enums.DepartmentStatus;
import com.norton.backend.enums.GenderEnum;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.enums.PositionStatus;
import com.norton.backend.enums.UserStatus;
import com.norton.backend.models.DepartmentModel;
import com.norton.backend.models.EducationLevelModel;
import com.norton.backend.models.GeoCommuneModel;
import com.norton.backend.models.GeoDistrictModel;
import com.norton.backend.models.GeoProvinceModel;
import com.norton.backend.models.GeoVillageModel;
import com.norton.backend.models.OfficerAddressModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.PositionModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.DepartmentRepository;
import com.norton.backend.repositories.EducationLevelRepository;
import com.norton.backend.repositories.GeoCommuneRepository;
import com.norton.backend.repositories.GeoDistrictRepository;
import com.norton.backend.repositories.GeoProvinceRepository;
import com.norton.backend.repositories.GeoVillageRepository;
import com.norton.backend.repositories.OfficerAddressRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PositionRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private final EducationLevelRepository educationLevelRepository;
  private final GeoProvinceRepository geoProvinceRepository;
  private final GeoDistrictRepository geoDistrictRepository;
  private final GeoCommuneRepository geoCommuneRepository;
  private final GeoVillageRepository geoVillageRepository;
  private final OfficerAddressRepository officerAddressRepository;
  private final PositionRepository positionRepository;
  private final OfficerRepository officerRepository;
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    Map<String, DepartmentModel> departmentsByCode = loadOrCreateDepartments();
    Map<String, PositionModel> positionsByCode = loadOrCreatePositions(departmentsByCode);
    Map<String, EducationLevelModel> educationLevelsByName = loadOrCreateEducationLevels();
    GeoSeedContext geoSeedContext = loadOrCreateGeographies();
    UserRoleModel officerRole = loadOfficerRole();

    List<OfficerSeed> officers = buildOfficerSeeds();
    for (OfficerSeed seed : officers) {
      PositionModel position = positionsByCode.get(seed.seededPositionCode().toUpperCase());
      if (position == null) {
        throw new RuntimeException("Position code not found: " + seed.seededPositionCode());
      }

      EducationLevelModel educationLevel = educationLevelsByName.get(seed.educationLevelName());
      if (educationLevel == null) {
        throw new RuntimeException("Education level not found: " + seed.educationLevelName());
      }

      OfficerModel existingOfficer =
          officerRepository.findByOfficerCode(seed.officerCode()).orElse(null);
      if (existingOfficer != null) {
        patchOfficer(existingOfficer, seed, position, educationLevel);
        officerRepository.save(existingOfficer);
        seedAddressesIfMissing(existingOfficer, seed, geoSeedContext);
        continue;
      }

      UserModel user = loadOrCreateUser(seed, officerRole);
      if (user.getOfficer() != null) {
        user = createDedicatedUser(seed, officerRole);
      }
      OfficerModel officer = buildOfficer(seed, position, educationLevel, user);
      officer = officerRepository.save(officer);
      seedAddressesIfMissing(officer, seed, geoSeedContext);
    }

    removeStaleOrganizationSeeds();

    System.out.println("Officer Khmer seed data inserted/updated successfully.");
  }

  private Map<String, DepartmentModel> loadOrCreateDepartments() {
    Map<String, DepartmentModel> departmentsByCode = new HashMap<>();
    for (DepartmentModel department : departmentRepository.findAll()) {
      buildDepartmentSeeds().stream()
          .filter(seed -> seed.name().equals(department.getName()))
          .findFirst()
          .ifPresent(seed -> departmentsByCode.put(seed.code().toUpperCase(), department));
    }

    for (DepartmentSeed seed : buildDepartmentSeeds()) {
      String key = seed.code().toUpperCase();
      if (departmentsByCode.containsKey(key)) {
        continue;
      }

      DepartmentModel department =
          DepartmentModel.builder()
              .name(seed.name())
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

  private Map<String, EducationLevelModel> loadOrCreateEducationLevels() {
    Map<String, EducationLevelModel> educationLevelsByName = new HashMap<>();
    for (EducationLevelModel educationLevel : educationLevelRepository.findAll()) {
      educationLevelsByName.put(educationLevel.getName(), educationLevel);
    }

    for (EducationLevelSeed seed : buildEducationLevelSeeds()) {
      if (educationLevelsByName.containsKey(seed.name())) {
        continue;
      }

      EducationLevelModel educationLevel =
          EducationLevelModel.builder().name(seed.name()).description(seed.description()).build();
      educationLevel = educationLevelRepository.save(educationLevel);
      educationLevelsByName.put(seed.name(), educationLevel);
    }
    return educationLevelsByName;
  }

  private GeoSeedContext loadOrCreateGeographies() {
    Map<String, GeoProvinceModel> provincesByCode = loadOrCreateProvinces();
    Map<String, GeoDistrictModel> districtsByCode = loadOrCreateDistricts(provincesByCode);
    Map<String, GeoCommuneModel> communesByCode = loadOrCreateCommunes(districtsByCode);
    Map<String, GeoVillageModel> villagesByCode = loadOrCreateVillages(communesByCode);
    return new GeoSeedContext(provincesByCode, districtsByCode, communesByCode, villagesByCode);
  }

  private Map<String, GeoProvinceModel> loadOrCreateProvinces() {
    Map<String, GeoProvinceModel> provincesByCode = new HashMap<>();
    for (GeoProvinceModel province : geoProvinceRepository.findAll()) {
      provincesByCode.put(province.getCode(), province);
    }
    for (GeoProvinceSeed seed : buildProvinceSeeds()) {
      GeoProvinceModel province =
          provincesByCode.computeIfAbsent(
              seed.code(),
              code ->
                  geoProvinceRepository.save(
                      GeoProvinceModel.builder()
                          .code(code)
                          .nameEn(seed.nameEn())
                          .nameKh(seed.nameKh())
                          .build()));
      province.setNameEn(seed.nameEn());
      province.setNameKh(seed.nameKh());
      geoProvinceRepository.save(province);
    }
    return provincesByCode;
  }

  private Map<String, GeoDistrictModel> loadOrCreateDistricts(
      Map<String, GeoProvinceModel> provincesByCode) {
    Map<String, GeoDistrictModel> districtsByCode = new HashMap<>();
    for (GeoDistrictModel district : geoDistrictRepository.findAll()) {
      districtsByCode.put(district.getCode(), district);
    }
    for (GeoDistrictSeed seed : buildDistrictSeeds()) {
      GeoProvinceModel province = provincesByCode.get(seed.provinceCode());
      if (province == null) {
        throw new RuntimeException("Province code not found: " + seed.provinceCode());
      }
      GeoDistrictModel district =
          districtsByCode.computeIfAbsent(
              seed.code(),
              code ->
                  geoDistrictRepository.save(
                      GeoDistrictModel.builder()
                          .province(province)
                          .code(code)
                          .nameEn(seed.nameEn())
                          .nameKh(seed.nameKh())
                          .build()));
      district.setProvince(province);
      district.setNameEn(seed.nameEn());
      district.setNameKh(seed.nameKh());
      geoDistrictRepository.save(district);
    }
    return districtsByCode;
  }

  private Map<String, GeoCommuneModel> loadOrCreateCommunes(
      Map<String, GeoDistrictModel> districtsByCode) {
    Map<String, GeoCommuneModel> communesByCode = new HashMap<>();
    for (GeoCommuneModel commune : geoCommuneRepository.findAll()) {
      communesByCode.put(commune.getCode(), commune);
    }
    for (GeoCommuneSeed seed : buildCommuneSeeds()) {
      GeoDistrictModel district = districtsByCode.get(seed.districtCode());
      if (district == null) {
        throw new RuntimeException("District code not found: " + seed.districtCode());
      }
      GeoCommuneModel commune =
          communesByCode.computeIfAbsent(
              seed.code(),
              code ->
                  geoCommuneRepository.save(
                      GeoCommuneModel.builder()
                          .district(district)
                          .code(code)
                          .nameEn(seed.nameEn())
                          .nameKh(seed.nameKh())
                          .build()));
      commune.setDistrict(district);
      commune.setNameEn(seed.nameEn());
      commune.setNameKh(seed.nameKh());
      geoCommuneRepository.save(commune);
    }
    return communesByCode;
  }

  private Map<String, GeoVillageModel> loadOrCreateVillages(
      Map<String, GeoCommuneModel> communesByCode) {
    Map<String, GeoVillageModel> villagesByCode = new HashMap<>();
    for (GeoVillageModel village : geoVillageRepository.findAll()) {
      villagesByCode.put(village.getCode(), village);
    }
    for (GeoVillageSeed seed : buildVillageSeeds()) {
      GeoCommuneModel commune = communesByCode.get(seed.communeCode());
      if (commune == null) {
        throw new RuntimeException("Commune code not found: " + seed.communeCode());
      }
      GeoVillageModel village =
          villagesByCode.computeIfAbsent(
              seed.code(),
              code ->
                  geoVillageRepository.save(
                      GeoVillageModel.builder()
                          .commune(commune)
                          .code(code)
                          .nameEn(seed.nameEn())
                          .nameKh(seed.nameKh())
                          .build()));
      village.setCommune(commune);
      village.setNameEn(seed.nameEn());
      village.setNameKh(seed.nameKh());
      geoVillageRepository.save(village);
    }
    return villagesByCode;
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
                        .fullName(seed.firstNameEn() + " " + seed.lastNameEn())
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
            .fullName(seed.firstNameEn() + " " + seed.lastNameEn())
            .passwordHash(passwordEncoder.encode("officer123"))
            .role(officerRole)
            .userStatus(UserStatus.ACTIVE)
            .build());
  }

  private OfficerModel buildOfficer(
      OfficerSeed seed,
      PositionModel position,
      EducationLevelModel educationLevel,
      UserModel user) {
    return OfficerModel.builder()
        .uuid(UUID.randomUUID().toString())
        .officerCode(seed.officerCode())
        .firstNameEn(seed.firstNameEn())
        .lastNameEn(seed.lastNameEn())
        .firstNameKh(seed.firstNameKh())
        .lastNameKh(seed.lastNameKh())
        .gender(seed.gender())
        .dateOfBirth(seed.dateOfBirth())
        .nationalId(seed.nationalId())
        .nationality(seed.nationality())
        .ethnicity(seed.ethnicity())
        .phone(seed.phone())
        .email(seed.officerEmail())
        .office(position.getDepartment())
        .position(position)
        .educationLevel(educationLevel)
        .hireDate(seed.hireDate())
        .contractType(seed.contractType())
        .status(OfficerStatus.ACTIVE)
        .user(user)
        .build();
  }

  private void patchOfficer(
      OfficerModel officer,
      OfficerSeed seed,
      PositionModel position,
      EducationLevelModel educationLevel) {
    officer.setFirstNameEn(seed.firstNameEn());
    officer.setLastNameEn(seed.lastNameEn());
    officer.setFirstNameKh(seed.firstNameKh());
    officer.setLastNameKh(seed.lastNameKh());
    officer.setGender(seed.gender());
    officer.setDateOfBirth(seed.dateOfBirth());
    officer.setNationalId(seed.nationalId());
    officer.setNationality(seed.nationality());
    officer.setEthnicity(seed.ethnicity());
    officer.setPhone(seed.phone());
    officer.setEmail(seed.officerEmail());
    officer.setOffice(position.getDepartment());
    officer.setPosition(position);
    officer.setEducationLevel(educationLevel);
    officer.setHireDate(seed.hireDate());
    officer.setContractType(seed.contractType());
    officer.setStatus(OfficerStatus.ACTIVE);

    if (officer.getUser() != null) {
      officer.getUser().setFullName(seed.firstNameEn() + " " + seed.lastNameEn());
      officer.getUser().setEmail(seed.userEmail());
      userRepository.save(officer.getUser());
    }
  }

  private void seedAddressesIfMissing(
      OfficerModel officer, OfficerSeed seed, GeoSeedContext geoSeedContext) {
    if (!officerAddressRepository.findByOfficer_Id(officer.getId()).isEmpty()) {
      return;
    }

    for (AddressSeed addressSeed : buildAddressSeeds(seed)) {
      officerAddressRepository.save(
          OfficerAddressModel.builder()
              .officer(officer)
              .addressType(addressSeed.addressType())
              .province(resolveProvince(geoSeedContext, addressSeed.provinceCode()))
              .district(resolveDistrict(geoSeedContext, addressSeed.districtCode()))
              .commune(resolveCommune(geoSeedContext, addressSeed.communeCode()))
              .village(resolveVillage(geoSeedContext, addressSeed.villageCode()))
              .streetAndHomeNumber(addressSeed.streetAndHomeNumber())
              .primary(addressSeed.primary())
              .build());
    }
  }

  private List<EducationLevelSeed> buildEducationLevelSeeds() {
    return List.of(
        new EducationLevelSeed("មធ្យមសិក្សាទុតិយភូមិ", "បានបញ្ចប់ការអប់រំមធ្យមសិក្សាទុតិយភូមិ"),
        new EducationLevelSeed("បរិញ្ញាបត្ររង", "បានបញ្ចប់ការអប់រំកម្រិតបរិញ្ញាបត្ររង"),
        new EducationLevelSeed("បរិញ្ញាបត្រ", "បានបញ្ចប់ការអប់រំកម្រិតបរិញ្ញាបត្រ"),
        new EducationLevelSeed("អនុបណ្ឌិត", "បានបញ្ចប់ការអប់រំកម្រិតអនុបណ្ឌិត"),
        new EducationLevelSeed("បណ្ឌិត", "បានបញ្ចប់ការអប់រំកម្រិតបណ្ឌិត"));
  }

  private GeoProvinceModel resolveProvince(GeoSeedContext context, String code) {
    GeoProvinceModel province = context.provincesByCode().get(code);
    if (province == null) {
      throw new RuntimeException("Province code not found: " + code);
    }
    return province;
  }

  private GeoDistrictModel resolveDistrict(GeoSeedContext context, String code) {
    GeoDistrictModel district = context.districtsByCode().get(code);
    if (district == null) {
      throw new RuntimeException("District code not found: " + code);
    }
    return district;
  }

  private GeoCommuneModel resolveCommune(GeoSeedContext context, String code) {
    GeoCommuneModel commune = context.communesByCode().get(code);
    if (commune == null) {
      throw new RuntimeException("Commune code not found: " + code);
    }
    return commune;
  }

  private GeoVillageModel resolveVillage(GeoSeedContext context, String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    GeoVillageModel village = context.villagesByCode().get(code);
    if (village == null) {
      throw new RuntimeException("Village code not found: " + code);
    }
    return village;
  }

  private List<AddressSeed> buildAddressSeeds(OfficerSeed seed) {
    int number = seed.seedNumber();
    String homeNumber = "#" + (100 + number) + ", Street " + (200 + number);

    return List.of(
        new AddressSeed(
            "current", "12", "1205", "120501", "12050101", homeNumber + ", Phnom Penh", true),
        new AddressSeed(
            "permanent",
            "03",
            "0302",
            "030201",
            "03020101",
            "Family home " + number + ", Kampong Cham",
            false),
        new AddressSeed(
            "birthplace",
            "05",
            "0503",
            "050301",
            "05030101",
            "Birth village record " + number,
            false));
  }

  private List<GeoProvinceSeed> buildProvinceSeeds() {
    return List.of(
        new GeoProvinceSeed("12", "Phnom Penh", "ភ្នំពេញ"),
        new GeoProvinceSeed("03", "Kampong Cham", "កំពង់ចាម"),
        new GeoProvinceSeed("05", "Kampong Speu", "កំពង់ស្ពឺ"));
  }

  private List<GeoDistrictSeed> buildDistrictSeeds() {
    return List.of(
        new GeoDistrictSeed("1205", "12", "Chamkar Mon", "ចំការមន"),
        new GeoDistrictSeed("0302", "03", "Kampong Siem", "កំពង់សៀម"),
        new GeoDistrictSeed("0503", "05", "Samraong Tong", "សំរោងទង"));
  }

  private List<GeoCommuneSeed> buildCommuneSeeds() {
    return List.of(
        new GeoCommuneSeed("120501", "1205", "Boeung Keng Kang Ti Muoy", "បឹងកេងកងទី១"),
        new GeoCommuneSeed("030201", "0302", "Ampil", "អំពិល"),
        new GeoCommuneSeed("050301", "0503", "Tang Krouch", "តាំងក្រូច"));
  }

  private List<GeoVillageSeed> buildVillageSeeds() {
    return List.of(
        new GeoVillageSeed("12050101", "120501", "Village 1", "ភូមិ១"),
        new GeoVillageSeed("03020101", "030201", "Ampil Village", "ភូមិអំពិល"),
        new GeoVillageSeed("05030101", "050301", "Tang Krouch Village", "ភូមិតាំងក្រូច"));
  }

  private void removeStaleOrganizationSeeds() {
    Set<String> activeOfficeNames = new HashSet<>();
    for (DepartmentSeed seed : buildDepartmentSeeds()) {
      activeOfficeNames.add(seed.name());
    }

    Set<String> activePositionCodes = new HashSet<>();
    for (PositionSeed seed : buildPositionSeeds()) {
      activePositionCodes.add(seed.code().toUpperCase());
    }

    for (PositionModel position : positionRepository.findAll()) {
      String code = position.getCode() == null ? "" : position.getCode().toUpperCase();
      if (!activePositionCodes.contains(code)
          && officerRepository.countByPosition_Id(position.getId()) == 0) {
        positionRepository.delete(position);
      }
    }

    for (DepartmentModel office : departmentRepository.findAll()) {
      if (!activeOfficeNames.contains(office.getName())
          && positionRepository.countByDepartment_Id(office.getId()) == 0
          && officerRepository.countByOffice_Id(office.getId()) == 0) {
        departmentRepository.delete(office);
      }
    }
  }

  private List<DepartmentSeed> buildDepartmentSeeds() {
    return List.of(
        new DepartmentSeed("DEP-01", "គណៈអភិបាល", "", "គណៈអភិបាល"),
        new DepartmentSeed("DEP-02", "គណៈនាយករដ្ឋបាល", "", "គណៈនាយករដ្ឋបាល"),
        new DepartmentSeed(
            "DEP-03", "ការិយាល័យរដ្ឋបាល និងបុគ្គលិក", "", "ការិយាល័យរដ្ឋបាល និងបុគ្គលិក"),
        new DepartmentSeed(
            "DEP-04", "ការិយាល័យផែនការ និងហិរញ្ញវត្ថុ", "", "ការិយាល័យផែនការ និងហិរញ្ញវត្ថុ"),
        new DepartmentSeed("DEP-05", "អង្គភាពលទ្ធកម្ម", "", "អង្គភាពលទ្ធកម្ម"),
        new DepartmentSeed(
            "DEP-06", "ការិយាល័យលេខាធិការក្រុមប្រឹក្សា", "", "ការិយាល័យលេខាធិការក្រុមប្រឹក្សា"),
        new DepartmentSeed(
            "DEP-07", "ការិយាល័យអប់រំ យុវជន និងកីឡា", "", "ការិយាល័យអប់រំ យុវជន និងកីឡា"),
        new DepartmentSeed(
            "DEP-08",
            "ការិយាល័យរៀបចំដែនដី នគរូបនីយកម្ម សំណង់ និងភូមិបាល",
            "",
            "ការិយាល័យរៀបចំដែនដី នគរូបនីយកម្ម សំណង់ និងភូមិបាល"),
        new DepartmentSeed(
            "DEP-09",
            "ការិយាល័យច្បាប់ និងសម្រុះសម្រួលវិវាទមូលដ្ឋាន",
            "",
            "ការិយាល័យច្បាប់ និងសម្រុះសម្រួលវិវាទមូលដ្ឋាន"),
        new DepartmentSeed(
            "DEP-10",
            "ការិយាល័យសាធារណការ ដឹកជញ្ជូន អនាម័យ បរិស្ថាន និងសណ្តាប់ធ្នាប់សាធារណៈ",
            "",
            "ការិយាល័យសាធារណការ ដឹកជញ្ជូន អនាម័យ បរិស្ថាន និងសណ្តាប់ធ្នាប់សាធារណៈ"),
        new DepartmentSeed(
            "DEP-11",
            "ការិយាល័យសេដ្ឋកិច្ច និងអភិវឌ្ឍន៍សហគមន៍",
            "",
            "ការិយាល័យសេដ្ឋកិច្ច និងអភិវឌ្ឍន៍សហគមន៍"),
        new DepartmentSeed(
            "DEP-12",
            "ការិយាល័យសង្គមកិច្ច និងសុខុមាលភាពសង្គម",
            "",
            "ការិយាល័យសង្គមកិច្ច និងសុខុមាលភាពសង្គម"),
        new DepartmentSeed("DEP-13", "ការិយាល័យច្រកចេញចូលតែមួយ", "", "ការិយាល័យច្រកចេញចូលតែមួយ"));
  }

  private List<PositionSeed> buildPositionSeeds() {
    List<PositionNameSeed> governorPositions =
        List.of(
            new PositionNameSeed("POS-01", "អភិបាល"), new PositionNameSeed("POS-02", "អភិបាលរង"));
    List<PositionNameSeed> managementPositions =
        List.of(
            new PositionNameSeed("POS-03", "នាយករដ្ឋបាល"),
            new PositionNameSeed("POS-04", "នាយករងរដ្ឋបាល"));
    List<PositionNameSeed> officePositions =
        List.of(
            new PositionNameSeed("POS-05", "ប្រធានការិយាល័យ"),
            new PositionNameSeed("POS-06", "អនុប្រធានការិយាល័យ"),
            new PositionNameSeed("POS-07", "មន្ត្រី"),
            new PositionNameSeed("POS-08", "មន្ត្រីកិច្ចសន្យា"));

    List<PositionSeed> positions = new ArrayList<>();
    for (DepartmentSeed department : buildDepartmentSeeds()) {
      List<PositionNameSeed> positionNames =
          switch (department.code()) {
            case "DEP-01" -> governorPositions;
            case "DEP-02" -> managementPositions;
            default -> officePositions;
          };
      for (PositionNameSeed position : positionNames) {
        positions.add(
            new PositionSeed(
                position.code() + "-" + department.code(),
                position.name(),
                department.code(),
                position.name()));
      }
    }
    return positions;
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

  private record PositionNameSeed(String code, String name) {}

  private record EducationLevelSeed(String name, String description) {}

  private record GeoSeedContext(
      Map<String, GeoProvinceModel> provincesByCode,
      Map<String, GeoDistrictModel> districtsByCode,
      Map<String, GeoCommuneModel> communesByCode,
      Map<String, GeoVillageModel> villagesByCode) {}

  private record GeoProvinceSeed(String code, String nameEn, String nameKh) {}

  private record GeoDistrictSeed(String code, String provinceCode, String nameEn, String nameKh) {}

  private record GeoCommuneSeed(String code, String districtCode, String nameEn, String nameKh) {}

  private record GeoVillageSeed(String code, String communeCode, String nameEn, String nameKh) {}

  private record AddressSeed(
      String addressType,
      String provinceCode,
      String districtCode,
      String communeCode,
      String villageCode,
      String streetAndHomeNumber,
      Boolean primary) {}

  private record OfficerSeed(
      String officerCode,
      String username,
      String userEmail,
      String officerEmail,
      String firstName,
      String lastName,
      GenderEnum gender,
      String phone,
      String positionCode) {

    String firstNameEn() {
      return "Officer";
    }

    String lastNameEn() {
      return String.format("Seed %03d", seedNumber());
    }

    String firstNameKh() {
      return firstName;
    }

    String lastNameKh() {
      return lastName;
    }

    LocalDate dateOfBirth() {
      int number = seedNumber();
      return LocalDate.of(1984 + (number % 16), 1 + (number % 12), 1 + (number % 27));
    }

    String nationalId() {
      return String.format("KH%09d", seedNumber());
    }

    String nationality() {
      return "Cambodian";
    }

    String ethnicity() {
      return "Cambodian";
    }

    String educationLevelName() {
      return switch (seedNumber() % 5) {
        case 0 -> "អនុបណ្ឌិត";
        case 1 -> "បរិញ្ញាបត្រ";
        case 2 -> "បរិញ្ញាបត្ររង";
        case 3 -> "មធ្យមសិក្សាទុតិយភូមិ";
        default -> "បណ្ឌិត";
      };
    }

    LocalDate hireDate() {
      int number = seedNumber();
      return LocalDate.of(2018 + (number % 7), 1 + (number % 12), 1 + (number % 27));
    }

    String contractType() {
      return seedNumber() % 4 == 0 ? "Contract" : "Permanent";
    }

    String seededPositionCode() {
      int number = seedNumber();
      if (number <= 2) {
        return String.format("POS-%02d-DEP-01", number);
      }
      if (number <= 4) {
        return String.format("POS-%02d-DEP-02", number);
      }
      int positionNumber = ((number - 5) % 4) + 5;
      int officeNumber = ((number - 5) % 11) + 3;
      return String.format("POS-%02d-DEP-%02d", positionNumber, officeNumber);
    }

    int seedNumber() {
      String digits = officerCode.replaceAll("\\D", "");
      return digits.isBlank() ? 1 : Integer.parseInt(digits);
    }
  }
}
