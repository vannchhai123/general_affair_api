package com.norton.backend.seeds;

import com.norton.backend.enums.DepartmentStatus;
import com.norton.backend.enums.GenderEnum;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.enums.PositionStatus;
import com.norton.backend.models.*;
import com.norton.backend.repositories.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
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

  @Override
  public void run(String... args) {

    if (officerRepository.count() > 0) return;

    UserModel adminUser =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new RuntimeException("Admin user not found"));

    UserModel normalUser =
        userRepository
            .findByUsername("user")
            .orElseThrow(() -> new RuntimeException("User not found"));

    UserModel officerUser =
        userRepository
            .findByUsername("Kelly")
            .orElseThrow(() -> new RuntimeException("User not found"));

    UserModel officerUser1 =
        userRepository
            .findByUsername("vannchhai")
            .orElseThrow(() -> new RuntimeException("User not found"));

    DepartmentModel hr =
        DepartmentModel.builder().name("Human Resources").status(DepartmentStatus.ACTIVE).build();

    DepartmentModel it =
        DepartmentModel.builder().name("IT Department").status(DepartmentStatus.ACTIVE).build();

    departmentRepository.saveAll(List.of(hr, it));

    PositionModel hrManager =
        PositionModel.builder()
            .name("HR Manager")
            .department(hr)
            .status(PositionStatus.ACTIVE)
            .build();

    PositionModel developer =
        PositionModel.builder()
            .name("Software Developer")
            .department(it)
            .status(PositionStatus.ACTIVE)
            .build();

    positionRepository.saveAll(List.of(hrManager, developer));

    OfficerModel officer1 =
        OfficerModel.builder()
            .uuid(UUID.randomUUID().toString())
            .officerCode("OFF-001")
            .firstName("John")
            .lastName("Doe")
            .gender(GenderEnum.MALE)
            .phone("012345678")
            .email("officer1@gmail.com")
            .imageUrl("https://example.com/images/john.jpg")
            .position(hrManager)
            .status(OfficerStatus.ACTIVE)
            .user(adminUser)
            .build();

    OfficerModel officer2 =
        OfficerModel.builder()
            .uuid(UUID.randomUUID().toString())
            .officerCode("OFF-002")
            .firstName("Jane")
            .lastName("Smith")
            .gender(GenderEnum.MALE)
            .phone("098765432")
            .email("officer2@gmail.com")
            .imageUrl("https://example.com/images/jane.jpg")
            .position(developer)
            .status(OfficerStatus.ACTIVE)
            .user(normalUser)
            .build();

    OfficerModel officer3 =
        OfficerModel.builder()
            .uuid(UUID.randomUUID().toString())
            .officerCode("OFF-004")
            .firstName("ឈិន")
            .lastName("ខិលី")
            .gender(GenderEnum.MALE)
            .phone("098765432")
            .email("chhenkelly123@gmail.com")
            .imageUrl("https://example.com/images/jane.jpg")
            .position(developer)
            .status(OfficerStatus.ACTIVE)
            .user(officerUser)
            .build();

    OfficerModel officer4 =
        OfficerModel.builder()
            .uuid(UUID.randomUUID().toString())
            .officerCode("OFF-005")
            .firstName("វ៉ានឆៃ")
            .lastName("ឆាន")
            .gender(GenderEnum.MALE)
            .phone("098765432")
            .email("vannchhai123@gmail.com")
            .imageUrl("https://example.com/images/jane.jpg")
            .position(developer)
            .status(OfficerStatus.ACTIVE)
            .user(officerUser1)
            .build();

    officerRepository.saveAll(List.of(officer1, officer2, officer3, officer4));

    System.out.println("✅ Officer seed data inserted successfully!");
  }
}
