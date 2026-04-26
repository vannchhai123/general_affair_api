package com.norton.backend.repositories;

import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.models.AttendanceModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceModel, Long> {

  @Query(
      """
      SELECT COUNT(a)
      FROM Attendance a
      LEFT JOIN a.status s
      WHERE UPPER(COALESCE(s.code, '')) = UPPER(:statusCode)
      """)
  long countByStatusCodeIgnoreCase(String statusCode);

  boolean existsByOfficerIdAndDate(Long officerId, java.time.LocalDate date);

  java.util.Optional<AttendanceModel> findByOfficerOfficerCodeAndDate(
      String officerCode, java.time.LocalDate date);

  java.util.List<AttendanceModel> findAllByOfficerUserIdAndDateBetween(
      Long userId, java.time.LocalDate fromDate, java.time.LocalDate toDate);

  java.util.List<AttendanceModel> findAllByOfficerIdAndDateBetween(
      Long officerId, java.time.LocalDate fromDate, java.time.LocalDate toDate);

  java.util.Optional<AttendanceModel> findByOfficerIdAndDate(
      Long officerId, java.time.LocalDate date);

  @Query(
      """
      SELECT a
      FROM Attendance a
      JOIN FETCH a.officer o
      LEFT JOIN FETCH a.status
      WHERE o.user.id = :userId
        AND a.date = :date
      """)
  java.util.Optional<AttendanceModel> findByOfficerUserIdAndDate(
      Long userId, java.time.LocalDate date);

  @Query(
      """
      SELECT a
      FROM Attendance a
      JOIN FETCH a.officer o
      JOIN FETCH o.position p
      JOIN FETCH p.department
      LEFT JOIN FETCH a.status
      LEFT JOIN FETCH a.approvedBy
      WHERE a.id = :id
      """)
  java.util.Optional<AttendanceModel> findByIdWithDetails(Long id);

  @Query(
      """
    SELECT new com.norton.backend.dto.responses.attendances.AttendanceResponse(
        a.id,
        o.id,
        o.firstName,
        o.lastName,
        o.position.department.name,
        o.officerCode,
        o.imageUrl,
        a.date,
        a.checkIn,
        a.checkOut,
        a.totalWorkMin,
        a.totalLateMin,
        s.name
    )
    FROM Attendance a
    JOIN a.officer o
    LEFT JOIN a.status s
""")
  Page<AttendanceResponse> findAllAttendance(Pageable pageable);
}
