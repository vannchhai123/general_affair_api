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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
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
  private final JdbcTemplate jdbcTemplate;

  @Override
  public void run(String... args) {
    // Clean up existing officers and non-default users in a database-agnostic way
    try {
      // Try PostgreSQL/MySQL style truncate cascade
      jdbcTemplate.execute("TRUNCATE TABLE officers CASCADE");
      jdbcTemplate.execute("TRUNCATE TABLE invitations CASCADE");
    } catch (Exception e) {
      // Fallback to H2 style truncate / set referential integrity false
      try {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE qr_session_checkins");
        jdbcTemplate.execute("TRUNCATE TABLE qr_session_logs");
        jdbcTemplate.execute("TRUNCATE TABLE qr_sessions");
        jdbcTemplate.execute("TRUNCATE TABLE attendance_sessions");
        jdbcTemplate.execute("TRUNCATE TABLE attendance");
        jdbcTemplate.execute("TRUNCATE TABLE document_logs");
        jdbcTemplate.execute("TRUNCATE TABLE document_files");
        jdbcTemplate.execute("TRUNCATE TABLE documents");
        jdbcTemplate.execute("TRUNCATE TABLE invitation_participants");
        jdbcTemplate.execute("TRUNCATE TABLE invitations");
        jdbcTemplate.execute("TRUNCATE TABLE officer_permissions");
        jdbcTemplate.execute("TRUNCATE TABLE officer_addresses");
        jdbcTemplate.execute("TRUNCATE TABLE officers");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
      } catch (Exception ex) {
        // Fallback to sequential deletes in case referential integrity cannot be set
        jdbcTemplate.execute("DELETE FROM qr_session_checkins");
        jdbcTemplate.execute("DELETE FROM qr_session_logs");
        jdbcTemplate.execute("DELETE FROM qr_sessions");
        jdbcTemplate.execute("DELETE FROM attendance_sessions");
        jdbcTemplate.execute("DELETE FROM attendance");
        jdbcTemplate.execute("DELETE FROM document_logs");
        jdbcTemplate.execute("DELETE FROM document_files");
        jdbcTemplate.execute("DELETE FROM documents");
        jdbcTemplate.execute("DELETE FROM invitation_participants");
        jdbcTemplate.execute("DELETE FROM invitations");
        jdbcTemplate.execute("DELETE FROM officer_permissions");
        jdbcTemplate.execute("DELETE FROM officer_addresses");
        jdbcTemplate.execute("DELETE FROM officers");
      }
    }

    try {
      jdbcTemplate.execute("DELETE FROM audit_log");
    } catch (Exception e) {
      System.out.println("Audit log table not present or could not be cleared.");
    }
    try {
      jdbcTemplate.execute("DELETE FROM reports");
    } catch (Exception e) {
      System.out.println("Reports table not present or could not be cleared.");
    }

    jdbcTemplate.execute(
        "DELETE FROM users WHERE LOWER(username) NOT IN ('admin', 'headoffice', 'manager', 'kelly', 'vannchhai', 'banned')");

    Map<String, DepartmentModel> departmentsByCode = loadOrCreateDepartments();
    Map<String, PositionModel> positionsByCode = loadOrCreatePositions(departmentsByCode);
    Map<String, EducationLevelModel> educationLevelsByName = loadOrCreateEducationLevels();
    GeoSeedContext geoSeedContext = loadOrCreateGeographies();
    UserRoleModel officerRole = loadOfficerRole();

    List<OfficerSeed> officers = buildOfficerSeeds();
    for (OfficerSeed seed : officers) {
      String normalizedPositionCode = normalizePositionCode(seed.positionCode());
      PositionModel position = positionsByCode.get(normalizedPositionCode);
      if (position == null) {
        throw new RuntimeException("Position code not found: " + normalizedPositionCode);
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

      UserRoleModel targetRole =
          userRoleRepository.findByRoleName(seed.roleName()).orElse(officerRole);
      UserModel user = loadOrCreateUser(seed, targetRole);
      if (user.getOfficer() != null) {
        user = createDedicatedUser(seed, targetRole);
      }
      OfficerModel officer = buildOfficer(seed, position, educationLevel, user);
      officer = officerRepository.save(officer);
      seedAddressesIfMissing(officer, seed, geoSeedContext);
    }

    removeStaleOrganizationSeeds();

    // Link an officer profile to vannchhai
    userRepository
        .findByUsername("vannchhai")
        .ifPresent(
            user -> {
              OfficerModel officer =
                  officerRepository.findByUserIdWithPosition(user.getId()).orElse(null);
              PositionModel position =
                  positionRepository.findAll().stream().findFirst().orElse(null);
              if (position != null) {
                if (officer == null) {
                  officer =
                      OfficerModel.builder().uuid(UUID.randomUUID().toString()).user(user).build();
                }
                officer.setOfficerCode("OFF-999");
                officer.setFirstNameEn("Vannchhai");
                officer.setLastNameEn("Developer");
                officer.setFirstNameKh("វ៉ាន់ឆៃ");
                officer.setLastNameKh("ឆាន");
                officer.setGender(GenderEnum.MALE);
                officer.setDateOfBirth(LocalDate.of(1995, 1, 1));
                officer.setNationalId("999999999");
                officer.setNationality("ខ្មែរ");
                officer.setEthnicity("ខ្មែរ");
                officer.setPhone("012345678");
                officer.setEmail("vannchhai@gmail.com");
                officer.setOffice(position.getDepartment());
                officer.setPosition(position);
                officer.setEducationLevel(
                    educationLevelRepository.findAll().stream().findFirst().orElse(null));
                officer.setHireDate(LocalDate.of(2020, 1, 1));
                officer.setStatus(OfficerStatus.ACTIVE);

                officerRepository.save(officer);
                System.out.println("✅ Seeded/Updated officer profile for user vannchhai.");
              }
            });

    System.out.println("Officer Khmer seed data inserted/updated successfully.");
  }

  private Map<String, DepartmentModel> loadOrCreateDepartments() {
    Map<String, DepartmentModel> departmentsByCode = new HashMap<>();
    for (DepartmentModel department : departmentRepository.findAll()) {
      buildDepartmentSeeds().stream()
          .filter(seed -> seed.name().equals(department.getName()))
          .findFirst()
          .ifPresent(
              seed -> {
                departmentsByCode.put(seed.code().toUpperCase(), department);
                if (department.getNameKh() == null) {
                  department.setNameKh(seed.name());
                  departmentRepository.save(department);
                }
              });
    }

    for (DepartmentSeed seed : buildDepartmentSeeds()) {
      String key = seed.code().toUpperCase();
      if (departmentsByCode.containsKey(key)) {
        continue;
      }

      DepartmentModel department =
          DepartmentModel.builder()
              .name(seed.name())
              .nameKh(seed.name())
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
        positionsByCode.put(normalizePositionCode(position.getCode()), position);
      }
    }

    for (PositionSeed seed : buildPositionSeeds()) {
      String key = normalizePositionCode(seed.code());
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

  private static String normalizePositionCode(String positionCode) {
    return positionCode == null ? null : positionCode.trim().toUpperCase();
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
    UserModel user = userRepository.findByUsername(seed.username()).orElse(null);
    if (user == null) {
      user =
          UserModel.builder()
              .uuid(UUID.randomUUID())
              .username(seed.username())
              .email(seed.userEmail())
              .fullName(seed.firstNameEn() + " " + seed.lastNameEn())
              .passwordHash(passwordEncoder.encode("officer@1234"))
              .role(officerRole)
              .userStatus(UserStatus.ACTIVE)
              .build();
      user = userRepository.save(user);
    } else {
      user.setPasswordHash(passwordEncoder.encode("officer@1234"));
      user.setRole(officerRole);
      user.setFullName(seed.firstNameEn() + " " + seed.lastNameEn());
      user.setEmail(seed.userEmail());
      user = userRepository.save(user);
    }
    return user;
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
            .passwordHash(passwordEncoder.encode("officer@1234"))
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
        .invitationPriority(seed.invitationPriority())
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
    officer.setInvitationPriority(seed.invitationPriority());

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
            new PositionNameSeed("POS-08", "មន្ត្រីកិច្ចសន្យា"),
            new PositionNameSeed("POS-09", "មន្ត្រីជំនាញ"),
            new PositionNameSeed("POS-10", "មន្ត្រីអនុវត្ត"));

    List<PositionSeed> positions = new ArrayList<>();
    for (DepartmentSeed department : buildDepartmentSeeds()) {
      List<PositionNameSeed> positionNames = new ArrayList<>();
      switch (department.code()) {
        case "DEP-01" -> {
          positionNames.addAll(governorPositions);
          positionNames.addAll(officePositions);
        }
        case "DEP-02" -> {
          positionNames.addAll(managementPositions);
          positionNames.addAll(officePositions);
        }
        default -> positionNames.addAll(officePositions);
      }
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

  private String mapDepartmentToCode(String csvDeptName) {
    if (csvDeptName == null) return null;
    String name = csvDeptName.trim();
    if (name.contains("គណៈអភិបាល")) return "DEP-01";
    if (name.contains("គណៈនាយករដ្ឋបាល")) return "DEP-02";
    if (name.contains("រដ្ឋបាល") && name.contains("បុគ្គលិក")) return "DEP-03";
    if (name.contains("ផែនការ") && name.contains("ហិរញ្ញវត្ថុ")) return "DEP-04";
    if (name.contains("លទ្ធកម្ម")) return "DEP-05";
    if (name.contains("លេខាធិការ") || name.contains("លេខាធិកា")) return "DEP-06";
    if (name.contains("អប់រំ")) return "DEP-07";
    if (name.contains("ដែនដី")) return "DEP-08";
    if (name.contains("សម្រុះសម្រួល") || name.contains("នីតិកម្ម") || name.contains("ច្បាប់"))
      return "DEP-09";
    if (name.contains("សាធារណៈការ") || name.contains("សាធារណការ")) return "DEP-10";
    if (name.contains("សេដ្ឋកិច្ច")) return "DEP-11";
    if (name.contains("សង្គមកិច្ច")) return "DEP-12";
    if (name.contains("ច្រកចេញចូល")) return "DEP-13";
    return null;
  }

  private String mapPositionToCode(String csvPosName, String deptCode) {
    if (csvPosName == null || deptCode == null) return null;
    String name = csvPosName.trim();
    if ("DEP-01".equals(deptCode)) {
      if (name.contains("អភិបាលរង")) return "POS-02-DEP-01";
      if (name.contains("អភិបាល")) return "POS-01-DEP-01";
    }
    if ("DEP-02".equals(deptCode)) {
      if (name.contains("នាយករង")) return "POS-04-DEP-02";
      if (name.contains("នាយក")) return "POS-03-DEP-02";
    }
    if (name.contains("ប្រធាន")) return "POS-05-" + deptCode;
    if (name.contains("អនុប្រធាន")) return "POS-06-" + deptCode;
    if (name.contains("មន្ត្រី")) return "POS-07-" + deptCode;
    return "POS-07-" + deptCode;
  }

  private String mapRoleName(String csvRole) {
    if (csvRole == null) return "ROLE_OFFICER";
    String role = csvRole.trim().toLowerCase();
    return switch (role) {
      case "manager" -> "ROLE_MANAGER";
      case "super admin" -> "ROLE_ADMIN";
      case "head-office" -> "ROLE_HEAD_OFFICE";
      default -> "ROLE_OFFICER";
    };
  }

  private String mapEducationLevel(String csvEdu) {
    if (csvEdu == null) return "មធ្យមសិក្សាទុតិយភូមិ";
    String edu = csvEdu.trim();
    if (edu.contains("បរិញ្ញាបត្ររង") || edu.contains("បរិញ្ញាប័ត្ររង")) return "បរិញ្ញាបត្ររង";
    if (edu.contains("បរិញ្ញាបត្រ") || edu.contains("បរិញ្ញាប័ត្រ")) return "បរិញ្ញាបត្រ";
    if (edu.contains("អនុបណ្ឌិត")) return "អនុបណ្ឌិត";
    if (edu.contains("បណ្ឌិត")) return "បណ្ឌិត";
    if (edu.contains("មធ្យមសិក្សា")) return "មធ្យមសិក្សាទុតិយភូមិ";
    return edu;
  }

  private List<OfficerSeed> buildOfficerSeeds() {
    List<OfficerSeed> seeds = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                new ClassPathResource("employees_final_184.csv").getInputStream(),
                StandardCharsets.UTF_8))) {

      String line = reader.readLine(); // read header
      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty()) {
          continue;
        }
        String[] parts = line.split(",", -1);
        if (parts.length < 16) {
          continue;
        }

        String officerCodeRaw = parts[0].trim();
        if (officerCodeRaw.isEmpty() || !officerCodeRaw.matches("\\d+")) {
          continue;
        }
        String lastNameKh = parts[2].trim();
        String firstNameKh = parts[3].trim();
        String lastNameEn = parts[4].trim();
        String firstNameEn = parts[5].trim();
        String genderStr = parts[6].trim();
        String dobStr = parts[7].trim();
        String departmentName = parts[10].trim();
        String positionName = parts[11].trim();
        String eduLevelRaw = parts[12].trim();
        String hireDateStr = parts[13].trim();
        String csvRole = parts[14].trim();

        int number = Integer.parseInt(officerCodeRaw);
        String officerCode = "OFF-" + String.format("%03d", number);

        String username = parts[15].trim();
        if (username.isEmpty()) {
          username = (firstNameEn + lastNameEn).replaceAll("\\s+", "").toLowerCase();
        }
        String userEmail = username + "@dummy.com";
        String officerEmail = username + ".officer@dummy.com";

        GenderEnum gender = "ប្រុស".equals(genderStr) ? GenderEnum.MALE : GenderEnum.FEMALE;
        String phone = "09" + String.format("%08d", number % 100000000);

        LocalDate dateOfBirth;
        try {
          dateOfBirth = LocalDate.parse(dobStr, formatter);
        } catch (Exception e) {
          dateOfBirth = LocalDate.of(1980, 1, 1);
        }

        LocalDate hireDate;
        try {
          hireDate = LocalDate.parse(hireDateStr, formatter);
        } catch (Exception e) {
          hireDate = LocalDate.of(2010, 7, 7);
        }

        String deptCode = mapDepartmentToCode(departmentName);
        String positionCode = mapPositionToCode(positionName, deptCode);
        String roleName = mapRoleName(csvRole);
        String eduLevel = mapEducationLevel(eduLevelRaw);

        seeds.add(
            new OfficerSeed(
                officerCode,
                username,
                userEmail,
                officerEmail,
                firstNameKh,
                lastNameKh,
                gender,
                phone,
                positionCode,
                roleName,
                firstNameEn,
                lastNameEn,
                dateOfBirth,
                hireDate,
                eduLevel));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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
      String positionCode,
      String roleName,
      String firstNameEn,
      String lastNameEn,
      LocalDate dateOfBirth,
      LocalDate hireDate,
      String educationLevelName) {

    public String firstNameEn() {
      return firstNameEn;
    }

    public String lastNameEn() {
      return lastNameEn;
    }

    public String firstNameKh() {
      return firstName;
    }

    public String lastNameKh() {
      return lastName;
    }

    public String nationalId() {
      return String.format("KH%09d", seedNumber());
    }

    public String nationality() {
      return "ខ្មែរ";
    }

    public String ethnicity() {
      return "ខ្មែរ";
    }

    public boolean invitationPriority() {
      return java.util.List.of(
              "OFF-001", "OFF-002", "OFF-003", "OFF-004", "OFF-005", "OFF-006", "OFF-007")
          .contains(officerCode);
    }

    public int seedNumber() {
      String digits = officerCode.replaceAll("\\D", "");
      return digits.isBlank() ? 1 : Integer.parseInt(digits);
    }

    public String contractType() {
      return positionCode != null && positionCode.startsWith("POS-08") ? "CONTRACT" : "FULL_TIME";
    }
  }
}
