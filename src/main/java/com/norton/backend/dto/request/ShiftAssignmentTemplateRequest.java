package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ShiftAssignmentTemplateRequest {
  @NotNull(message = "id is required")
  @JsonAlias({"shiftId", "shift_id"})
  private Long id;

  @NotBlank(message = "scope is required")
  private String scope;

  @NotNull(message = "scopeId is required")
  @JsonAlias("scope_id")
  private Long scopeId;

  @JsonAlias("scope_name")
  @Size(max = 255, message = "scopeName must not exceed 255 characters")
  private String scopeName;

  @NotNull(message = "effectiveFrom is required")
  @JsonAlias("effective_from")
  private LocalDate effectiveFrom;

  @JsonAlias("effective_to")
  private LocalDate effectiveTo;

  @NotNull(message = "days is required")
  private Map<String, List<Long>> days;
}
