package com.norton.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "educational_levels")
public class EducationLevelModel extends BaseIdModel {

  @NotBlank(message = "Education level name is required")
  @Size(max = 100)
  @Column(nullable = false, unique = true, length = 100)
  private String name;

  private String description;
}
