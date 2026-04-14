package com.norton.backend.mapper;

import com.norton.backend.dto.responses.OfficerResponse;
import com.norton.backend.dto.responses.OfficerResponseDto;
import com.norton.backend.models.OfficerModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfficerMapper {

  @Mapping(target = "uuid", expression = "java(officer.getUuid())")
  @Mapping(source = "position.name", target = "positionName")
  @Mapping(source = "position.department.name", target = "departmentName")
  @Mapping(source = "status", target = "status")
  OfficerResponse toProfileResponse(OfficerModel officer);

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "officerCode", target = "officerCode")
  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "position.name", target = "position")
  @Mapping(source = "position.department.name", target = "department")
  OfficerResponseDto toResponse(OfficerModel model);
}
