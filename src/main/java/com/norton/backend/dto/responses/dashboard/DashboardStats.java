package com.norton.backend.dto.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStats {

  private OfficersStats officers;
  private AttendanceStats attendance;
  private InvitationsStats invitations;
  private MissionsStats missions;

  @JsonProperty("leave_requests")
  private LeaveRequestsStats leaveRequests;

  @JsonProperty("recent_attendance")
  private List<RecentAttendanceDto> recentAttendance;

  @Data
  @Builder
  public static class OfficersStats {
    private long total;
    private long active;

    @JsonProperty("on_leave")
    private long onLeave;

    private long inactive;
  }

  @Data
  @Builder
  public static class AttendanceStats {
    private long total;
    private long approved;
    private long pending;
    private long absent;
  }

  @Data
  @Builder
  public static class InvitationsStats {
    private long total;
    private long active;
    private long completed;
  }

  @Data
  @Builder
  public static class MissionsStats {
    private long total;
    private long approved;
    private long pending;
  }

  @Data
  @Builder
  public static class LeaveRequestsStats {
    private long total;
    private long approved;
    private long pending;
  }
}
