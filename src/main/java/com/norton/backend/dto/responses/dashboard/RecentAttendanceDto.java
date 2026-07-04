package com.norton.backend.dto.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentAttendanceDto {
  private Long id;
  private String date;
  private String status;

  @JsonProperty("total_work_minutes")
  private Integer totalWorkMinutes;

  @JsonProperty("total_late_minutes")
  private Integer totalLateMinutes;

  private OfficerDto officer;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OfficerDto {
    private Long id;

    @JsonProperty("first_name_kh")
    private String firstNameKh;

    @JsonProperty("last_name_kh")
    private String lastNameKh;

    @JsonProperty("first_name_en")
    private String firstNameEn;

    @JsonProperty("last_name_en")
    private String lastNameEn;

    private String position;
    private String department;
  }
}
