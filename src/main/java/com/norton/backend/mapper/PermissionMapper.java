package com.norton.backend.mapper;

import com.norton.backend.dto.request.PermissionRequest;
import com.norton.backend.dto.responses.permissions.PermissionResponse;
import com.norton.backend.models.PermissionModel;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

  @Mapping(target = "permissionName", source = "permissionName")
  @Mapping(target = "code", source = "permissionName")
  @Mapping(
      target = "name",
      expression =
          "java(permission.getDescription() != null ? permission.getDescription() : permission.getPermissionName())")
  PermissionResponse toResponse(PermissionModel permission);

  List<PermissionResponse> toResponseList(List<PermissionModel> permissions);

  PermissionModel toEntity(PermissionRequest request);
}
