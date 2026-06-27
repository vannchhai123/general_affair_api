package com.norton.backend.mapper;

import com.norton.backend.dto.responses.officers.OfficerResponse;
import com.norton.backend.dto.responses.officers.OfficerResponseDto;
import com.norton.backend.dto.responses.officers.OfficerStatsResponse;
import com.norton.backend.models.OfficerModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfficerMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(target = "uuid", expression = "java(officer.getUuid())")
  @Mapping(source = "position.name", target = "positionName")
  @Mapping(source = "office.name", target = "departmentName")
  @Mapping(source = "status", target = "status")
  OfficerResponse toProfileResponse(OfficerModel officer);

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "office.id", target = "officeId")
  @Mapping(source = "position.id", target = "positionId")
  @Mapping(source = "educationLevel.id", target = "educationLevelId")
  @Mapping(source = "officerCode", target = "officerCode")
  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "position.name", target = "position")
  @Mapping(source = "office.name", target = "department")
  @Mapping(source = "firstNameKh", target = "firstNameKh")
  @Mapping(source = "lastNameKh", target = "lastNameKh")
  @Mapping(
      target = "sex",
      expression =
          "java(model.getGender() != null ? model.getGender().name().toLowerCase(java.util.Locale.ROOT) : null)")
  OfficerResponseDto toResponse(OfficerModel model);

  default OfficerStatsResponse toStatsResponse(
      long total, long active, long inactive, long onLeave) {
    return OfficerStatsResponse.builder()
        .totalElements(total)
        .activeCount(active)
        .inactiveCount(inactive)
        .onLeaveCount(onLeave)
        .build();
  }
}
