package com.norton.backend.repositories;

import com.norton.backend.models.QrSessionCheckInModel;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QrSessionCheckInRepository extends JpaRepository<QrSessionCheckInModel, Long> {
  long countByQrSessionToken(String token);

  @Query(
      """
      SELECT c
      FROM QrSessionCheckInModel c
      JOIN FETCH c.officer o
      JOIN FETCH o.position p
      JOIN FETCH p.department
      JOIN FETCH c.qrSession q
      WHERE q.token = :token
      ORDER BY c.scannedAt DESC
      """)
  List<QrSessionCheckInModel> findAllByQrSessionTokenWithOfficer(String token);

  @Query(
      """
      SELECT c
      FROM QrSessionCheckInModel c
      JOIN FETCH c.officer o
      JOIN FETCH o.position p
      JOIN FETCH p.department
      JOIN FETCH c.qrSession q
      WHERE c.scannedAt >= :startOfDay AND c.scannedAt < :endOfDay
      ORDER BY c.scannedAt DESC
      """)
  List<QrSessionCheckInModel> findAllTodayWithOfficer(
      @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
