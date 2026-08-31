package com.norton.backend.seeds;

import com.norton.backend.models.AuditLogModel;
import com.norton.backend.repositories.AuditLogRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@DependsOn("userDataLoading")
@RequiredArgsConstructor
@Order(6)
public class AuditLogDataLoading implements CommandLineRunner {

  private final AuditLogRepository auditLogRepository;

  @Override
  public void run(String... args) {
    if (auditLogRepository.count() == 0) {
      AuditLogModel log1 =
          AuditLogModel.builder()
              .actorId(1L)
              .actorName("Super Admin")
              .actorEmail("superadmin@domain.gov.kh")
              .action("STATUS_CHANGE")
              .entityType("User")
              .entityId(2L)
              .ipAddress("192.168.1.100")
              .userAgent(
                  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
              .details("Changed status of user 'governor.kandal' to ACTIVE")
              .stateBefore("{\"status\":\"INACTIVE\"}")
              .stateAfter("{\"status\":\"ACTIVE\"}")
              .timestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
              .build();

      AuditLogModel log2 =
          AuditLogModel.builder()
              .actorId(1L)
              .actorName("Super Admin")
              .actorEmail("superadmin@domain.gov.kh")
              .action("ROLE_ASSIGN")
              .entityType("User")
              .entityId(3L)
              .ipAddress("192.168.1.100")
              .userAgent(
                  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
              .details("Assigned role 'ROLE_GOVERNOR' to user 'sokha.chea'")
              .stateBefore("{\"roleId\":6,\"roleCode\":\"ROLE_OFFICER\"}")
              .stateAfter("{\"roleId\":2,\"roleCode\":\"ROLE_GOVERNOR\"}")
              .timestamp(Instant.now().minus(2, ChronoUnit.HOURS))
              .build();

      AuditLogModel log3 =
          AuditLogModel.builder()
              .actorId(1L)
              .actorName("Super Admin")
              .actorEmail("superadmin@domain.gov.kh")
              .action("PASSWORD_RESET")
              .entityType("User")
              .entityId(4L)
              .ipAddress("192.168.1.100")
              .userAgent(
                  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
              .details("Super Admin initiated password reset for user 'dara.som'")
              .stateBefore(null)
              .stateAfter(null)
              .timestamp(Instant.now().minus(5, ChronoUnit.HOURS))
              .build();

      auditLogRepository.save(log1);
      auditLogRepository.save(log2);
      auditLogRepository.save(log3);
      System.out.println("✅ Seeded sample audit logs successfully!");
    }
  }
}
