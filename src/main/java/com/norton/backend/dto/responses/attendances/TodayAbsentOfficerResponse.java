package com.norton.backend.dto.responses.attendances;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayAbsentOfficerResponse {

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
  private LocalDate date;
  private String status;
}
