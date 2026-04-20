package com.norton.backend.mapper;

import com.norton.backend.dto.responses.officers.OfficerPermissionResponse;
import com.norton.backend.models.OfficerPermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfficerPermissionMapper {

  @Mapping(source = "officer.id", target = "officerId")
  @Mapping(source = "permission.id", target = "permissionId")
  @Mapping(source = "officer.user.fullName", target = "officerName")
  @Mapping(source = "officer.position.name", target = "officerDepartment")
  @Mapping(source = "permission.permissionName", target = "permissionName")
  @Mapping(source = "permission.category", target = "permissionCategory")
  OfficerPermissionResponse toDto(OfficerPermission entity);

  @Mapping(source = "officer.id", target = "officerId")
  @Mapping(source = "permission.id", target = "permissionId")
  @Mapping(source = "officer.user.fullName", target = "officerName")
  @Mapping(source = "officer.position.name", target = "officerDepartment")
  @Mapping(source = "permission.permissionName", target = "permissionName")
  @Mapping(source = "permission.category", target = "permissionCategory")
  OfficerPermissionResponse toResponse(OfficerPermission entity);
}
