package com.norton.backend.seeds;

import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.OfficerPermission;
import com.norton.backend.models.PermissionModel;
import com.norton.backend.repositories.OfficerPermissionRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PermissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Order(5)
public class OfficerPermissionDataLoading implements CommandLineRunner {

  private final OfficerPermissionRepository officerPermissionRepository;
  private final OfficerRepository officerRepository;
  private final PermissionRepository permissionRepository;

  @Override
  public void run(String... args) {

    if (officerPermissionRepository.count() > 0) {
      return;
    }

    OfficerModel officer1 =
        officerRepository
            .findById(1L)
            .orElseThrow(() -> new RuntimeException("Officer 1 not found"));

    OfficerModel officer2 =
        officerRepository
            .findById(2L)
            .orElseThrow(() -> new RuntimeException("Officer 2 not found"));

    PermissionModel view =
        permissionRepository
            .findById(1L)
            .orElseThrow(() -> new RuntimeException("Permission 1 not found"));

    PermissionModel create =
        permissionRepository
            .findById(2L)
            .orElseThrow(() -> new RuntimeException("Permission 2 not found"));

    PermissionModel edit =
        permissionRepository
            .findById(3L)
            .orElseThrow(() -> new RuntimeException("Permission 3 not found"));

    List<OfficerPermission> data =
        List.of(
            OfficerPermission.builder()
                .officer(officer1)
                .permission(view)
                .grantedAt(LocalDateTime.parse("2026-01-15T08:00:00"))
                .grantedBy(1L)
                .build(),
            OfficerPermission.builder()
                .officer(officer1)
                .permission(create)
                .grantedAt(LocalDateTime.parse("2026-01-15T08:00:00"))
                .grantedBy(1L)
                .build(),
            OfficerPermission.builder()
                .officer(officer1)
                .permission(edit)
                .grantedAt(LocalDateTime.parse("2026-01-15T08:00:00"))
                .grantedBy(1L)
                .build(),
            OfficerPermission.builder()
                .officer(officer2)
                .permission(view)
                .grantedAt(LocalDateTime.parse("2026-01-20T09:00:00"))
                .grantedBy(1L)
                .build());

    officerPermissionRepository.saveAll(data);

    System.out.println("✅ OfficerPermission seeded successfully!");
  }
}
