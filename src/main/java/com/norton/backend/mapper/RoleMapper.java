package com.norton.backend.mapper;

import com.norton.backend.dto.responses.role.RoleResponse;
import com.norton.backend.dto.responses.role.RoleSimpleResponse;
import com.norton.backend.models.UserRoleModel;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {PermissionMapper.class})
public interface RoleMapper {

  default String normalize(String roleName) {
    if (roleName == null) {
      return null;
    }
    return roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName;
  }

  @Mapping(
      target = "userCount",
      expression = "java(role.getUsers() != null ? role.getUsers().size() : 0)")
  RoleResponse toResponse(UserRoleModel role);

  List<RoleResponse> toResponseList(List<UserRoleModel> roles);

  RoleSimpleResponse toSimpleResponse(UserRoleModel role);

  List<RoleSimpleResponse> toSimpleResponseList(List<UserRoleModel> roles);
}
