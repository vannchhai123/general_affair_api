package com.norton.backend.mapper;

import com.norton.backend.dto.responses.UserDto;
import com.norton.backend.models.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "role", source = "role.roleName")
  UserDto toDto(UserModel user);
}
