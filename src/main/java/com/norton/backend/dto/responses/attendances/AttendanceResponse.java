package com.norton.backend.dto.responses.attendances;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class AttendanceResponse {

  private Long id;
  private Long officerId;
  private String firstName;
  private String lastName;
  private String department;
  private String officerCode;

  private LocalDate date;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;

  private Integer totalWorkMin;
  private Integer totalLateMin;

  private String status;

  public AttendanceResponse(
      Long id,
      Long officerId,
      String firstName,
      String lastName,
      String department,
      String officerCode,
      LocalDate date,
      LocalDateTime checkIn,
      LocalDateTime checkOut,
      Integer totalWorkMin,
      Integer totalLateMin,
      String status) {
    this(
        id,
        officerId,
        firstName,
        lastName,
        department,
        officerCode,
        date,
        checkIn,
        checkOut,
        totalWorkMin,
        totalLateMin,
        status,
        null);
  }

  public AttendanceResponse(
      Long id,
      Long officerId,
      String firstName,
      String lastName,
      String department,
      String officerCode,
      LocalDate date,
      LocalDateTime checkIn,
      LocalDateTime checkOut,
      Integer totalWorkMin,
      Integer totalLateMin,
      String status,
      List<AttendanceSessionResponse> sessions) {
    this.id = id;
    this.officerId = officerId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.department = department;
    this.officerCode = officerCode;
    this.date = date;
    this.checkIn = checkIn;
    this.checkOut = checkOut;
    this.totalWorkMin = totalWorkMin;
    this.totalLateMin = totalLateMin;
    this.status = status;
    this.sessions = sessions;
  }

  private List<AttendanceSessionResponse> sessions;
}
