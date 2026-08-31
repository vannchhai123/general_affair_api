package com.norton.backend.dto.responses.superadmin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthResponse {

  private String status;

  private HealthComponentsDto components;

  private long uptime;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class HealthComponentsDto {
    private DatabaseHealthDto db;
    private RedisHealthDto redis;
    private DiskSpaceHealthDto diskSpace;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DatabaseHealthDto {
    private String status;

    @JsonProperty("latencyMs")
    private long latencyMs;

    @JsonProperty("poolActive")
    private int poolActive;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RedisHealthDto {
    private String status;

    @JsonProperty("latencyMs")
    private long latencyMs;

    @JsonProperty("memoryUsedMb")
    private long memoryUsedMb;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DiskSpaceHealthDto {
    private String status;

    @JsonProperty("freeGb")
    private long freeGb;

    @JsonProperty("totalGb")
    private long totalGb;
  }
}
