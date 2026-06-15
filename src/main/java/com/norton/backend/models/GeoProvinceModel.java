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
@Table(name = "geo_provinces")
public class GeoProvinceModel extends BaseIdModel {

  @NotBlank(message = "Province code is required")
  @Size(max = 10)
  @Column(nullable = false, unique = true, length = 10)
  private String code;

  @NotBlank(message = "Province English name is required")
  @Size(max = 100)
  @Column(name = "name_en", nullable = false, length = 100)
  private String nameEn;

  @NotBlank(message = "Province Khmer name is required")
  @Size(max = 100)
  @Column(name = "name_kh", nullable = false, length = 100)
  private String nameKh;
}
