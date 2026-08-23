package com.norton.backend.seeds;

import com.norton.backend.models.AttendanceLocationModel;
import com.norton.backend.models.AttendanceLocationSettingModel;
import com.norton.backend.models.embeddable.LatLngPoint;
import com.norton.backend.repositories.AttendanceLocationSettingRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(5)
@Profile("dev")
public class AttendanceLocationDataLoading implements CommandLineRunner {

  private final AttendanceLocationSettingRepository repository;

  @Override
  public void run(String... args) {
    if (repository.count() == 0) {
      AttendanceLocationSettingModel setting =
          AttendanceLocationSettingModel.builder()
              .isGlobalEnabled(true)
              .locations(new ArrayList<>())
              .build();

      List<LatLngPoint> senSokBoundary =
          List.of(
              new LatLngPoint(11.577559787876368, 104.86977117431823),
              new LatLngPoint(11.577574239876993, 104.87020435106328),
              new LatLngPoint(11.577258923330090, 104.87022446763041),
              new LatLngPoint(11.577243157493417, 104.86979397309427));

      AttendanceLocationModel senSokLocation =
          AttendanceLocationModel.builder()
              .name("Sen Sok Office")
              .setting(setting)
              .boundary(senSokBoundary)
              .build();

      List<LatLngPoint> nortonBoundary =
          List.of(
              new LatLngPoint(11.588065, 104.929502),
              new LatLngPoint(11.588776, 104.929757),
              new LatLngPoint(11.588460, 104.930682),
              new LatLngPoint(11.587693, 104.930529));

      AttendanceLocationModel nortonLocation =
          AttendanceLocationModel.builder()
              .name("Norton University")
              .setting(setting)
              .boundary(nortonBoundary)
              .build();

      setting.getLocations().add(senSokLocation);
      setting.getLocations().add(nortonLocation);

      repository.save(setting);
      System.out.println("✅ Attendance Location Settings seeded successfully!");
    }
  }
}
