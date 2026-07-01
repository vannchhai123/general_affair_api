package com.norton.backend.services.officer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.request.CreateOfficerRequest;
import com.norton.backend.enums.GenderEnum;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.mapper.OfficerMapper;
import com.norton.backend.mapper.UserMapper;
import com.norton.backend.models.DepartmentModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.PositionModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.DepartmentRepository;
import com.norton.backend.repositories.EducationLevelRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PositionRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import com.norton.backend.services.security.OfficeAccessService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class OfficerServiceImplTest {

  @Mock private UserMapper userMapper;
  @Mock private OfficerRepository officerRepository;
  @Mock private OfficerMapper officerMapper;
  @Mock private DepartmentRepository departmentRepository;
  @Mock private EducationLevelRepository educationLevelRepository;
  @Mock private PositionRepository positionRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private OfficeAccessService officeAccessService;

  @InjectMocks private OfficerServiceImpl officerService;

  @Test
  void updateOfficer_updatesInvitationPriority() {
    // Arrange
    Long officerId = 1L;

    OfficerModel officer = new OfficerModel();
    officer.setId(officerId);
    officer.setOfficerCode("OFF001");
    officer.setGender(GenderEnum.MALE);
    officer.setStatus(OfficerStatus.ACTIVE);
    officer.setInvitationPriority(false); // Initial value is false

    UserModel user = new UserModel();
    user.setId(10L);
    user.setUsername("old.username");
    user.setEmail("old@example.com");
    officer.setUser(user);

    DepartmentModel department = new DepartmentModel();
    department.setId(5L);
    department.setName("IT");
    officer.setOffice(department);

    PositionModel position = new PositionModel();
    position.setId(6L);
    position.setName("Developer");
    position.setDepartment(department);
    officer.setPosition(position);

    CreateOfficerRequest request = new CreateOfficerRequest();
    request.setOfficerCode("OFF001");
    request.setUsername("new.username");
    request.setFirstNameEn("John");
    request.setLastNameEn("Doe");
    request.setFirstNameKh("ចន");
    request.setLastNameKh("ដូ");
    request.setSex("MALE");
    request.setStatus("ACTIVE");
    request.setPhone("012345678");
    request.setHireDate(LocalDate.now());
    request.setOfficeId(5L);
    request.setPositionId(6L);
    request.setInvitationPriority(true); // Updating to true

    when(officerRepository.findByIdWithPosition(officerId)).thenReturn(Optional.of(officer));
    when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
    when(positionRepository.findById(6L)).thenReturn(Optional.of(position));
    when(officerRepository.save(any(OfficerModel.class))).thenAnswer(i -> i.getArgument(0));

    // Act
    officerService.updateOfficer(officerId, request);

    // Assert
    assertTrue(officer.isInvitationPriority()); // Verify that the model was updated to true
    verify(officerRepository).save(officer);
  }
}
