package com.norton.backend.repositories;

import com.norton.backend.enums.MeetingStatus;
import com.norton.backend.models.MeetingModel;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<MeetingModel, Long> {

  long countByAssigneeId(Long assigneeId);

  long countByAssigneeIdAndMeetingDate(Long assigneeId, LocalDate meetingDate);

  long countByAssigneeIdAndStatus(Long assigneeId, MeetingStatus status);

  List<MeetingModel> findTop5ByAssigneeIdOrderByMeetingDateDescMeetingTimeDescIdDesc(
      Long assigneeId, Pageable pageable);

  @Query(
      "SELECT m.meetingDate AS date, COUNT(m) AS meetingCount "
          + "FROM MeetingModel m "
          + "WHERE m.assigneeId = :assigneeId "
          + "AND m.meetingDate BETWEEN :startDate AND :endDate "
          + "GROUP BY m.meetingDate "
          + "ORDER BY m.meetingDate ASC")
  List<MeetingCalendarDateProjection> findMeetingCountsByAssigneeIdAndMeetingDateBetween(
      Long assigneeId, LocalDate startDate, LocalDate endDate);

  interface MeetingCalendarDateProjection {

    LocalDate getDate();

    Long getMeetingCount();
  }
}
