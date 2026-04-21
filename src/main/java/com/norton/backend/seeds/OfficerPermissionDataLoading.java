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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
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
            .findByOfficerCode("OFF-001")
            .orElseThrow(() -> new RuntimeException("Officer OFF-001 not found"));

    OfficerModel officer2 =
        officerRepository
            .findByOfficerCode("OFF-002")
            .orElseThrow(() -> new RuntimeException("Officer OFF-002 not found"));

    PermissionModel view =
        permissionRepository
            .findByPermissionName("OFFICER_VIEW")
            .orElseThrow(() -> new RuntimeException("Permission OFFICER_VIEW not found"));

    PermissionModel create =
        permissionRepository
            .findByPermissionName("OFFICER_CREATE")
            .orElseThrow(() -> new RuntimeException("Permission OFFICER_CREATE not found"));

    PermissionModel edit =
        permissionRepository
            .findByPermissionName("OFFICER_UPDATE")
            .orElseThrow(() -> new RuntimeException("Permission OFFICER_UPDATE not found"));

    List<OfficerPermission> data =
        List.of(
            OfficerPermission.builder()
                .officer(officer1)
                .permission(view)
                .grantedAt(LocalDateTime.parse("2026-01-15T08:00:00"))
                .grantedBy(officer1.getId())
                .build(),
            OfficerPermission.builder()
                .officer(officer1)
                .permission(create)
                .grantedAt(LocalDateTime.parse("2026-01-15T08:00:00"))
                .grantedBy(officer1.getId())
                .build(),
            OfficerPermission.builder()
                .officer(officer1)
                .permission(edit)
                .grantedAt(LocalDateTime.parse("2026-01-15T08:00:00"))
                .grantedBy(officer1.getId())
                .build(),
            OfficerPermission.builder()
                .officer(officer2)
                .permission(view)
                .grantedAt(LocalDateTime.parse("2026-01-20T09:00:00"))
                .grantedBy(officer1.getId())
                .build());

    officerPermissionRepository.saveAll(data);

    System.out.println("✅ OfficerPermission seeded successfully!");
  }
}
