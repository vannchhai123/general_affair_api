package com.norton.backend.repositories;

import com.norton.backend.models.AttendanceSessionModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSessionModel, Long> {

  java.util.Optional<AttendanceSessionModel> findByAttendanceIdAndShiftId(
      Long attendanceId, Long shiftId);

  List<AttendanceSessionModel> findByAttendanceId(Long attendanceId);

  @Query(
      """
      SELECT s
      FROM AttendanceSessionModel s
      LEFT JOIN FETCH s.shift
      WHERE s.attendance.id IN :attendanceIds
      ORDER BY s.id
      """)
  List<AttendanceSessionModel> findAllByAttendanceIds(List<Long> attendanceIds);
}
