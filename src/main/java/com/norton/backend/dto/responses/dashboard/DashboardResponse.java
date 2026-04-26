package com.norton.backend.dto.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

  private OfficersSummary officers;
  private AttendanceSummary attendance;
  private InvitationsSummary invitations;
  private ApprovalSummary missions;

  @JsonProperty("leave_requests")
  private ApprovalSummary leaveRequests;

  @JsonProperty("recent_attendance")
  private List<Object> recentAttendance;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OfficersSummary {
    private long total;
    private long active;

    @JsonProperty("on_leave")
    private long onLeave;

    private long inactive;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AttendanceSummary {
    private long total;
    private long approved;
    private long pending;
    private long absent;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InvitationsSummary {
    private long total;
    private long active;
    private long completed;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ApprovalSummary {
    private long total;
    private long approved;
    private long pending;
  }
}
