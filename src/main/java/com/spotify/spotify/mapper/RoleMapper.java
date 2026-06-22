package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.RoleRequest;
import com.spotify.spotify.dto.response.RoleResponse;
import com.spotify.spotify.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}