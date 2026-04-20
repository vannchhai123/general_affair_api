package com.norton.backend.dto.responses.attendances;

import lombok.Data;

@Data
public class AttendanceSessionResponse {
  private Long id;
  private String shiftName;
  private String checkIn;
  private String checkOut;
  private String status;
}
