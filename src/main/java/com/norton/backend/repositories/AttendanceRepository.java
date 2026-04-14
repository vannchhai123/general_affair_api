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
    SELECT new com.norton.backend.dto.responses.attendances.AttendanceResponse(
        a.id,
        o.id,
        o.firstName,
        o.lastName,
        o.position.department.name,
        o.officerCode,
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
