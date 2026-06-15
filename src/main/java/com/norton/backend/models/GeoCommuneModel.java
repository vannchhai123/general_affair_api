package com.norton.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "geo_communes")
public class GeoCommuneModel extends BaseIdModel {

  @NotNull(message = "District is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "district_id", nullable = false)
  private GeoDistrictModel district;

  @NotBlank(message = "Commune code is required")
  @Size(max = 10)
  @Column(nullable = false, unique = true, length = 10)
  private String code;

  @NotBlank(message = "Commune English name is required")
  @Size(max = 100)
  @Column(name = "name_en", nullable = false, length = 100)
  private String nameEn;

  @NotBlank(message = "Commune Khmer name is required")
  @Size(max = 100)
  @Column(name = "name_kh", nullable = false, length = 100)
  private String nameKh;
}
