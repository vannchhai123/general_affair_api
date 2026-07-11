package com.norton.backend.repositories;

import com.norton.backend.models.InvitationModel;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationModel, Long> {

  @Query(
      """
      select i from InvitationModel i
      join i.participants p
      where p.officer.id = :participantId
        and i.eventDate >= :startDate
        and i.eventDate <= :endDate
      order by i.eventDate asc, i.eventTime asc
      """)
  List<InvitationModel> findByParticipantIdAndEventDateBetween(
      @Param("participantId") Long participantId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
