package com.norton.backend.seeds;

import com.norton.backend.models.UserInvitationModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.UserInvitationRepository;
import com.norton.backend.repositories.UserRoleRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn("userDataLoading")
@RequiredArgsConstructor
@Order(5)
public class UserInvitationDataLoading implements CommandLineRunner {

  private final UserInvitationRepository invitationRepository;
  private final UserRoleRepository roleRepository;

  @Override
  public void run(String... args) {
    if (invitationRepository.count() == 0) {
      UserRoleModel governorRole =
          roleRepository
              .findByRoleName("ROLE_GOVERNOR")
              .orElseGet(() -> roleRepository.findAll().stream().findFirst().orElse(null));

      if (governorRole != null) {
        UserInvitationModel invitation =
            UserInvitationModel.builder()
                .email("mean.sokha@domain.gov.kh")
                .fullName("Sokha Mean")
                .khmerName("មាន សុខា")
                .role(governorRole)
                .status("PENDING")
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
        invitationRepository.save(invitation);
        System.out.println("✅ Seeded sample user invitation for mean.sokha@domain.gov.kh");
      }
    }
  }
}
