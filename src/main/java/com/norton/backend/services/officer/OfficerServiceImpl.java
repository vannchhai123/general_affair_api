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
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.PositionModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.DepartmentRepository;
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

    DepartmentModel department =
        departmentRepository
            .findByNameIgnoreCase(request.getDepartment())
            .orElseGet(
                () ->
                    departmentRepository.save(
                        DepartmentModel.builder()
                            .name(request.getDepartment().trim())
                            .status(DepartmentStatus.ACTIVE)
                            .build()));
    officeAccessService.assertCanAccessOffice(department.getId());

    PositionModel position =
        positionRepository
            .findByNameIgnoreCaseAndDepartment_NameIgnoreCase(
                request.getPosition(), request.getDepartment())
            .orElseGet(
                () ->
                    positionRepository.save(
                        PositionModel.builder()
                            .name(request.getPosition().trim())
                            .department(department)
                            .status(PositionStatus.ACTIVE)
                            .build()));

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
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .gender(parseGender(request.getSex()))
                .email(request.getEmail().trim())
                .phone(request.getPhone().trim())
                .status(parseOfficerStatus(request.getStatus()))
                .position(position)
                .user(user)
                .build());

    return CreateOfficerResponse.builder()
        .id(officer.getId())
        .userId(null)
        .officerCode(officer.getOfficerCode())
        .firstName(officer.getFirstName())
        .lastName(officer.getLastName())
        .sex(officer.getGender().name().toLowerCase(Locale.ROOT))
        .email(officer.getEmail())
        .position(officer.getPosition().getName())
        .department(officer.getPosition().getDepartment().getName())
        .phone(officer.getPhone())
        .status(officer.getStatus().name().toLowerCase(Locale.ROOT))
        .username(null)
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

    DepartmentModel department =
        departmentRepository
            .findByNameIgnoreCase(request.getDepartment())
            .orElseGet(
                () ->
                    departmentRepository.save(
                        DepartmentModel.builder()
                            .name(request.getDepartment().trim())
                            .status(DepartmentStatus.ACTIVE)
                            .build()));
    officeAccessService.assertCanAccessOffice(department.getId());

    PositionModel position =
        positionRepository
            .findByNameIgnoreCaseAndDepartment_NameIgnoreCase(
                request.getPosition(), request.getDepartment())
            .orElseGet(
                () ->
                    positionRepository.save(
                        PositionModel.builder()
                            .name(request.getPosition().trim())
                            .department(department)
                            .status(PositionStatus.ACTIVE)
                            .build()));

    officer.setOfficerCode(request.getOfficerCode().trim());
    officer.setFirstName(request.getFirstName().trim());
    officer.setLastName(request.getLastName().trim());
    officer.setGender(parseGender(request.getSex()));
    officer.setEmail(request.getEmail().trim());
    officer.setPhone(request.getPhone().trim());
    officer.setStatus(parseOfficerStatus(request.getStatus()));
    officer.setPosition(position);

    if (officer.getUser() != null) {
      UserModel user = officer.getUser();
      user.setEmail(request.getEmail().trim());
      user.setFullName((request.getFirstName().trim() + " " + request.getLastName().trim()).trim());
      userRepository.save(user);
    }

    OfficerModel updatedOfficer = officerRepository.save(officer);

    return CreateOfficerResponse.builder()
        .id(updatedOfficer.getId())
        .userId(null)
        .officerCode(updatedOfficer.getOfficerCode())
        .firstName(updatedOfficer.getFirstName())
        .lastName(updatedOfficer.getLastName())
        .sex(updatedOfficer.getGender().name().toLowerCase(Locale.ROOT))
        .email(updatedOfficer.getEmail())
        .position(updatedOfficer.getPosition().getName())
        .department(updatedOfficer.getPosition().getDepartment().getName())
        .phone(updatedOfficer.getPhone())
        .status(updatedOfficer.getStatus().name().toLowerCase(Locale.ROOT))
        .username(null)
        .build();
  }

  @Override
  public PageResponse<OfficerResponseDto> getAllOfficers(Pageable pageable) {

    Long officeId = officeAccessService.currentOfficeScopeIdOrNull();
    Page<OfficerModel> officer =
        officeId == null
            ? officerRepository.findAll(pageable)
            : officerRepository.findByPosition_Department_Id(officeId, pageable);
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
        officeId == null
            ? officerRepository.count()
            : officerRepository.countByPosition_Department_Id(officeId);

    long active =
        officeId == null
            ? officerRepository.countByStatus(OfficerStatus.ACTIVE)
            : officerRepository.countByStatusAndPosition_Department_Id(
                OfficerStatus.ACTIVE, officeId);
    long inactive =
        officeId == null
            ? officerRepository.countByStatus(OfficerStatus.INACTIVE)
            : officerRepository.countByStatusAndPosition_Department_Id(
                OfficerStatus.INACTIVE, officeId);
    long onLeave =
        officeId == null
            ? officerRepository.countByStatus(OfficerStatus.ON_LEAVE)
            : officerRepository.countByStatusAndPosition_Department_Id(
                OfficerStatus.ON_LEAVE, officeId);

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
}
