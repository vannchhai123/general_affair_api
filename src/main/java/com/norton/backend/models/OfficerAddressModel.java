package com.norton.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "officer_addresses")
public class OfficerAddressModel extends BaseIdModel {

  @NotNull(message = "Officer is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "officer_id", nullable = false)
  private OfficerModel officer;

  @Size(max = 50)
  @Column(name = "address_type", nullable = false, length = 50)
  private String addressType;

  @NotNull(message = "Province is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "province_id", nullable = false)
  private GeoProvinceModel province;

  @NotNull(message = "District is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "district_id", nullable = false)
  private GeoDistrictModel district;

  @NotNull(message = "Commune is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "commune_id", nullable = false)
  private GeoCommuneModel commune;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "village_id")
  private GeoVillageModel village;

  @Column(name = "street_and_home_number", columnDefinition = "text")
  private String streetAndHomeNumber;

  @Builder.Default
  @Column(name = "is_primary")
  private Boolean primary = true;
}
