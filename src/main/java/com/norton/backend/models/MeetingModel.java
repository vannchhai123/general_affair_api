package com.norton.backend.models;

import com.norton.backend.enums.MeetingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingModel extends BaseIdModel {

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "meeting_date", nullable = false)
  private LocalDate meetingDate;

  @Column(name = "meeting_time")
  private LocalTime meetingTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private MeetingStatus status;

  @Column(name = "assignee_id", nullable = false)
  private Long assigneeId;
}
