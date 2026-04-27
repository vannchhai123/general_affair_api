package com.norton.backend.dto.responses.attendances;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAttendanceResponse {
  private Long id;
  private Long officerId;
  private String imageUrl;
  private LocalDate date;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Integer totalWorkMin;
  private Integer totalLateMin;
  private String status;
  private String firstName;
  private String lastName;
  private String department;
  private String officerCode;
  private List<AttendanceSessionResponse> sessions;
}
