package com.norton.backend.models;

import com.norton.backend.models.embeddable.LatLngPoint;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendance_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLocationModel extends BaseIdModel {

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "setting_id")
  private AttendanceLocationSettingModel setting;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "attendance_location_boundaries",
      joinColumns = @JoinColumn(name = "location_id"))
  @OrderColumn(name = "point_order")
  @Builder.Default
  private List<LatLngPoint> boundary = new ArrayList<>();
}
