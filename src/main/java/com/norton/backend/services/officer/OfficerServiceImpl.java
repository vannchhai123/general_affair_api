package com.norton.backend.services.officer;

import com.norton.backend.dto.request.CreateOfficerRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.officers.CreateOfficerResponse;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.dto.responses.officers.OfficerResponseDto;
import com.norton.backend.dto.responses.officers.OfficerStatsResponse;
import com.norton.backend.enums.DepartmentStatus;
import com.norton.backend.enums.GenderEnum;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.enums.PositionStatus;
import com.norton.backend.enums.UserStatus;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.mapper.OfficerMapper;
import com.norton.backend.mapper.UserMapper;
import com.norton.backend.models.DepartmentModel;
import com.norton.backend.models.EducationLevelModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.PositionModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.DepartmentRepository;
import com.norton.backend.repositories.EducationLevelRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PositionRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import com.norton.backend.services.security.OfficeAccessService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfficerServiceImpl implements OfficerService {

  private final UserMapper userMapper;
  private final OfficerRepository officerRepository;
  private final OfficerMapper officerMapper;
  private final DepartmentRepository departmentRepository;
  private final EducationLevelRepository educationLevelRepository;
  private final PositionRepository positionRepository;
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;
  private final OfficeAccessService officeAccessService;

  @Override
  public MeResponse getMyProfile() {

    UserModel currentUser =
        (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    return userMapper.toMeResponse(currentUser);
  }

  @Override
  @Transactional
  public CreateOfficerResponse createOfficer(CreateOfficerRequest request) {
    if (officerRepository.existsByOfficerCode(request.getOfficerCode())) {
      throw new ConflictException("Officer code already exists: " + request.getOfficerCode());
    }

    if (officerRepository.existsByEmail(request.getEmail())) {
      throw new ConflictException("Officer email already exists: " + request.getEmail());
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ConflictException("User email already exists: " + request.getEmail());
    }

    DepartmentModel department = resolveOffice(request);
    officeAccessService.assertCanAccessOffice(department.getId());

    PositionModel position = resolvePosition(request, department);
    EducationLevelModel educationLevel = resolveEducationLevel(request.getEducationLevelId());

    UserRoleModel officerRole =
        userRoleRepository
            .findByRoleName("ROLE_OFFICER")
            .orElseThrow(() -> new ResourceNotFoundException("Role", "roleName", "ROLE_OFFICER"));

    String username = buildUniqueUsername(request);
    String fullName = request.getFirstName().trim() + " " + request.getLastName().trim();

    UserModel user =
        userRepository.save(
            UserModel.builder()
                .uuid(UUID.randomUUID())
                .username(username)
                .email(request.getEmail().trim())
                .fullName(fullName.trim())
                .passwordHash(passwordEncoder.encode(request.getOfficerCode().trim() + "@123"))
                .role(officerRole)
                .userStatus(UserStatus.ACTIVE)
                .build());

    OfficerModel officer =
        officerRepository.save(
            OfficerModel.builder()
                .officerCode(request.getOfficerCode().trim())
                .firstNameEn(request.getFirstName().trim())
                .lastNameEn(request.getLastName().trim())
                .firstNameKh(request.getFirstNameKh().trim())
                .lastNameKh(request.getLastNameKh().trim())
                .gender(parseGender(request.getSex()))
                .dateOfBirth(request.getDateOfBirth())
                .nationalId(trimToNull(request.getNationalId()))
                .nationality(defaultNationality(request.getNationality()))
                .ethnicity(defaultEthnicity(request.getEthnicity()))
                .email(request.getEmail().trim())
                .phone(request.getPhone().trim())
                .status(parseOfficerStatus(request.getStatus()))
                .office(department)
                .position(position)
                .educationLevel(educationLevel)
                .hireDate(request.getHireDate())
                .contractType(trimToNull(request.getContractType()))
                .user(user)
                .build());

    return CreateOfficerResponse.builder()
        .id(officer.getId())
        .userId(user.getId())
        .officeId(department.getId())
        .positionId(position.getId())
        .educationLevelId(educationLevel != null ? educationLevel.getId() : null)
        .officerCode(officer.getOfficerCode())
        .firstName(officer.getFirstName())
        .lastName(officer.getLastName())
        .firstNameKh(officer.getFirstNameKh())
        .lastNameKh(officer.getLastNameKh())
        .sex(officer.getGender().name().toLowerCase(Locale.ROOT))
        .dateOfBirth(officer.getDateOfBirth())
        .nationalId(officer.getNationalId())
        .nationality(officer.getNationality())
        .ethnicity(officer.getEthnicity())
        .email(officer.getEmail())
        .position(officer.getPosition().getName())
        .department(officer.getOffice().getName())
        .phone(officer.getPhone())
        .hireDate(officer.getHireDate())
        .contractType(officer.getContractType())
        .status(officer.getStatus().name().toLowerCase(Locale.ROOT))
        .username(user.getUsername())
        .build();
  }

  @Override
  @Transactional
  public CreateOfficerResponse updateOfficer(Long id, CreateOfficerRequest request) {
    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(id)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", id));
    officeAccessService.assertCanAccessOfficer(officer);

    if (officerRepository.existsByOfficerCodeAndIdNot(request.getOfficerCode(), id)) {
      throw new ConflictException("Officer code already exists: " + request.getOfficerCode());
    }

    if (officerRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
      throw new ConflictException("Officer email already exists: " + request.getEmail());
    }

    Long userId = officer.getUser() != null ? officer.getUser().getId() : null;
    if (userId != null && userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
      throw new ConflictException("User email already exists: " + request.getEmail());
    }

    DepartmentModel department = resolveOffice(request);
    officeAccessService.assertCanAccessOffice(department.getId());

    PositionModel position = resolvePosition(request, department);
    EducationLevelModel educationLevel = resolveEducationLevel(request.getEducationLevelId());

    officer.setOfficerCode(request.getOfficerCode().trim());
    officer.setFirstName(request.getFirstName().trim());
    officer.setLastName(request.getLastName().trim());
    officer.setFirstNameKh(request.getFirstNameKh().trim());
    officer.setLastNameKh(request.getLastNameKh().trim());
    officer.setGender(parseGender(request.getSex()));
    officer.setDateOfBirth(request.getDateOfBirth());
    officer.setNationalId(trimToNull(request.getNationalId()));
    officer.setNationality(defaultNationality(request.getNationality()));
    officer.setEthnicity(defaultEthnicity(request.getEthnicity()));
    officer.setEmail(request.getEmail().trim());
    officer.setPhone(request.getPhone().trim());
    officer.setStatus(parseOfficerStatus(request.getStatus()));
    officer.setOffice(department);
    officer.setPosition(position);
    officer.setEducationLevel(educationLevel);
    officer.setHireDate(request.getHireDate());
    officer.setContractType(trimToNull(request.getContractType()));

    if (officer.getUser() != null) {
      UserModel user = officer.getUser();
      user.setEmail(request.getEmail().trim());
      user.setFullName((request.getFirstName().trim() + " " + request.getLastName().trim()).trim());
      userRepository.save(user);
    }

    OfficerModel updatedOfficer = officerRepository.save(officer);

    return CreateOfficerResponse.builder()
        .id(updatedOfficer.getId())
        .userId(updatedOfficer.getUser() != null ? updatedOfficer.getUser().getId() : null)
        .officeId(department.getId())
        .positionId(position.getId())
        .educationLevelId(educationLevel != null ? educationLevel.getId() : null)
        .officerCode(updatedOfficer.getOfficerCode())
        .firstName(updatedOfficer.getFirstName())
        .lastName(updatedOfficer.getLastName())
        .firstNameKh(updatedOfficer.getFirstNameKh())
        .lastNameKh(updatedOfficer.getLastNameKh())
        .sex(updatedOfficer.getGender().name().toLowerCase(Locale.ROOT))
        .dateOfBirth(updatedOfficer.getDateOfBirth())
        .nationalId(updatedOfficer.getNationalId())
        .nationality(updatedOfficer.getNationality())
        .ethnicity(updatedOfficer.getEthnicity())
        .email(updatedOfficer.getEmail())
        .position(updatedOfficer.getPosition().getName())
        .department(updatedOfficer.getOffice().getName())
        .phone(updatedOfficer.getPhone())
        .hireDate(updatedOfficer.getHireDate())
        .contractType(updatedOfficer.getContractType())
        .status(updatedOfficer.getStatus().name().toLowerCase(Locale.ROOT))
        .username(updatedOfficer.getUser() != null ? updatedOfficer.getUser().getUsername() : null)
        .build();
  }

  @Override
  public PageResponse<OfficerResponseDto> getAllOfficers(Pageable pageable) {

    Long officeId = officeAccessService.currentOfficeScopeIdOrNull();
    Page<OfficerModel> officer =
        officeId == null
            ? officerRepository.findAll(pageable)
            : officerRepository.findByOffice_Id(officeId, pageable);
    List<OfficerResponseDto> content =
        officer.getContent().stream().map(officerMapper::toResponse).toList();

    return PageResponse.<OfficerResponseDto>builder()
        .content(content)
        .page(officer.getNumber())
        .size(officer.getSize())
        .totalElements(officer.getTotalElements())
        .totalPages(officer.getTotalPages())
        .last(officer.isLast())
        .build();
  }

  @Override
  public OfficerStatsResponse getOfficerStats() {
    Long officeId = officeAccessService.currentOfficeScopeIdOrNull();
    long total =
        officeId == null ? officerRepository.count() : officerRepository.countByOffice_Id(officeId);

    long active =
        officeId == null
            ? officerRepository.countByStatus(OfficerStatus.ACTIVE)
            : officerRepository.countByStatusAndOffice_Id(OfficerStatus.ACTIVE, officeId);
    long inactive =
        officeId == null
            ? officerRepository.countByStatus(OfficerStatus.INACTIVE)
            : officerRepository.countByStatusAndOffice_Id(OfficerStatus.INACTIVE, officeId);
    long onLeave =
        officeId == null
            ? officerRepository.countByStatus(OfficerStatus.ON_LEAVE)
            : officerRepository.countByStatusAndOffice_Id(OfficerStatus.ON_LEAVE, officeId);

    return officerMapper.toStatsResponse(total, active, inactive, onLeave);
  }

  private GenderEnum parseGender(String sex) {
    String normalized = sex == null ? "" : sex.trim().toUpperCase(Locale.ROOT);
    try {
      return GenderEnum.valueOf(normalized);
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Invalid sex. Expected one of: male, female");
    }
  }

  private OfficerStatus parseOfficerStatus(String status) {
    String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
    try {
      return OfficerStatus.valueOf(normalized);
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException(
          "Invalid status. Expected one of: active, inactive, on_leave, suspended");
    }
  }

  private String buildUniqueUsername(CreateOfficerRequest request) {
    String email =
        request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase(Locale.ROOT);
    String officerCode =
        request.getOfficerCode() == null
            ? ""
            : request.getOfficerCode().trim().toLowerCase(Locale.ROOT);

    String base = email.isBlank() ? officerCode.replace(" ", "") : email;
    if (base.isBlank()) {
      throw new BadRequestException("Unable to generate username");
    }

    String candidate = base;
    int suffix = 1;
    while (userRepository.existsByUsername(candidate)) {
      candidate = base + "_" + suffix;
      suffix++;
    }
    return candidate;
  }

  private EducationLevelModel resolveEducationLevel(Long educationLevelId) {
    if (educationLevelId == null) {
      return null;
    }
    return educationLevelRepository
        .findById(educationLevelId)
        .orElseThrow(() -> new ResourceNotFoundException("EducationLevel", "id", educationLevelId));
  }

  private DepartmentModel resolveOffice(CreateOfficerRequest request) {
    if (request.getOfficeId() != null) {
      return departmentRepository
          .findById(request.getOfficeId())
          .orElseThrow(() -> new ResourceNotFoundException("Office", "id", request.getOfficeId()));
    }
    if (request.getDepartment() == null || request.getDepartment().isBlank()) {
      throw new BadRequestException("office_id or office is required");
    }
    return departmentRepository
        .findByNameIgnoreCase(request.getDepartment())
        .orElseGet(
            () ->
                departmentRepository.save(
                    DepartmentModel.builder()
                        .name(request.getDepartment().trim())
                        .status(DepartmentStatus.ACTIVE)
                        .build()));
  }

  private PositionModel resolvePosition(CreateOfficerRequest request, DepartmentModel office) {
    if (request.getPositionId() != null) {
      PositionModel position =
          positionRepository
              .findById(request.getPositionId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Position", "id", request.getPositionId()));
      if (position.getDepartment() == null
          || !office.getId().equals(position.getDepartment().getId())) {
        throw new BadRequestException("Position does not belong to the selected office");
      }
      return position;
    }
    if (request.getPosition() == null || request.getPosition().isBlank()) {
      throw new BadRequestException("position_id or position is required");
    }
    return positionRepository
        .findByNameIgnoreCaseAndDepartment_NameIgnoreCase(request.getPosition(), office.getName())
        .orElseGet(
            () ->
                positionRepository.save(
                    PositionModel.builder()
                        .name(request.getPosition().trim())
                        .department(office)
                        .status(PositionStatus.ACTIVE)
                        .build()));
  }

  private String defaultNationality(String nationality) {
    String trimmed = trimToNull(nationality);
    return trimmed == null ? "Cambodian" : trimmed;
  }

  private String defaultEthnicity(String ethnicity) {
    String trimmed = trimToNull(ethnicity);
    return trimmed == null ? "Cambodian" : trimmed;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
