package com.norton.backend.mapper;

import com.norton.backend.dto.responses.UserDto;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.models.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {RoleMapper.class, OfficerMapper.class})
public interface UserMapper {

  @Mapping(target = "role", source = "role.roleName")
  UserDto toDto(UserModel user);

  @Mapping(target = "uuid", expression = "java(user.getUuid().toString())")
  @Mapping(target = "role", source = "role.roleName")
  @Mapping(target = "officer", source = "officer")
  MeResponse toMeResponse(UserModel user);
}
