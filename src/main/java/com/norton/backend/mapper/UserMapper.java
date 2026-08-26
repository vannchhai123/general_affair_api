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
  @Mapping(target = "roleNameKm", source = "role.nameKm")
  @Mapping(target = "roleNameEn", source = "role.nameEn")
  @Mapping(target = "hierarchyLevel", source = "role.hierarchyLevel")
  @Mapping(target = "permissions", expression = "java(mapAuthorities(user))")
  @Mapping(
      target = "imageUrl",
      expression =
          "java(user.getOfficer() != null ? user.getOfficer().getImageUrl() : user.getImageUrl())")
  UserDto toDto(UserModel user);

  @Mapping(target = "uuid", expression = "java(user.getUuid().toString())")
  @Mapping(target = "role", source = "role.roleName")
  @Mapping(target = "roleNameKm", source = "role.nameKm")
  @Mapping(target = "roleNameEn", source = "role.nameEn")
  @Mapping(target = "hierarchyLevel", source = "role.hierarchyLevel")
  @Mapping(target = "officerId", source = "officer.id")
  @Mapping(target = "officer", source = "officer")
  @Mapping(target = "permissions", expression = "java(mapAuthorities(user))")
  MeResponse toMeResponse(UserModel user);

  default java.util.List<java.lang.String> mapAuthorities(UserModel user) {
    if (user == null) {
      return java.util.Collections.emptyList();
    }
    return user.getAuthorities().stream()
        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
        .toList();
  }
}
