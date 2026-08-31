package com.norton.backend.repositories;

import com.norton.backend.models.AuditLogModel;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogModel, Long> {

  @Query(
      "SELECT a FROM AuditLogModel a WHERE "
          + "(:actorId IS NULL OR a.actorId = :actorId) "
          + "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) "
          + "AND (:entityType IS NULL OR LOWER(a.entityType) = LOWER(:entityType)) "
          + "AND (:startDate IS NULL OR a.timestamp >= :startDate) "
          + "AND (:endDate IS NULL OR a.timestamp <= :endDate) "
          + "ORDER BY a.timestamp DESC")
  List<AuditLogModel> searchAuditLogs(
      @Param("actorId") Long actorId,
      @Param("action") String action,
      @Param("entityType") String entityType,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);
}
