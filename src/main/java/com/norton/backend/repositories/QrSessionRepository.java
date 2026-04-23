package com.norton.backend.repositories;

import com.norton.backend.models.QrSessionModel;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QrSessionRepository extends JpaRepository<QrSessionModel, Long> {

  @Query(
      """
      SELECT q
      FROM QrSessionModel q
      LEFT JOIN FETCH q.createdBy
      WHERE q.token = :token
      """)
  Optional<QrSessionModel> findByTokenWithCreatedBy(String token);

  Optional<QrSessionModel> findTopBySessionDateAndShiftTypeOrderByIdDesc(
      LocalDate sessionDate, String shiftType);

  List<QrSessionModel> findAllBySessionDateOrderByStartsAtAsc(LocalDate sessionDate);

  List<QrSessionModel> findAllByStatusIgnoreCase(String status);
}
