package com.norton.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentUpsertRequest {
  @NotBlank(message = "name is required")
  @Size(max = 100, message = "name must not exceed 100 characters")
  private String name;

  @JsonProperty("name_kh")
  @JsonAlias({"nameKh", "name_kh"})
  @Size(max = 150, message = "Khmer name must not exceed 150 characters")
  private String nameKh;

  @Size(max = 50, message = "code must not exceed 50 characters")
  private String code;

  @Size(max = 255, message = "manager must not exceed 255 characters")
  private String manager;

  private Long adminId;

  @NotBlank(message = "status is required")
  private String status;

  @Size(max = 500, message = "description must not exceed 500 characters")
  private String description;
}
