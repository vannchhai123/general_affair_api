package com.norton.backend.dto.responses.attendances;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayPresentOfficerResponse {

  private Long id;
  private Long officerId;
  private String officerCode;
  private String firstNameKh;
  private String lastNameKh;
  private String firstNameEn;
  private String lastNameEn;
  private String department;
  private String position;
  private String phone;
  private String imageUrl;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDateTime checkIn;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDateTime checkOut;

  private Integer totalWorkMin;
  private Integer totalLateMin;
  private String status;
}
