package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.UserCreationRequest;
import com.spotify.spotify.dto.request.UserUpdateRequest;
import com.spotify.spotify.dto.response.UserResponse;
import com.spotify.spotify.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "email", source = "email")
    User toUser(UserCreationRequest request);
    @Mapping(target = "dob", source = "dob")
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
