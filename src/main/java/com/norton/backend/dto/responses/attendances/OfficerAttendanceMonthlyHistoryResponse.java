package com.norton.backend.dto.responses.attendances;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerAttendanceMonthlyHistoryResponse {

  private String month;
  private MonthlySummary summary;

  @JsonProperty("presentDates")
  private List<LocalDate> presentDates;

  @JsonProperty("absentDates")
  private List<LocalDate> absentDates;

  @JsonProperty("lateDates")
  private List<LocalDate> lateDates;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MonthlySummary {

    private Integer present;
    private Integer absent;
    private Integer late;
  }
}
