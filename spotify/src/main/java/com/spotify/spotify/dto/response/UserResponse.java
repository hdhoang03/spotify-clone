package com.spotify.spotify.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String name;
    String email;
    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate dob;
    Boolean enabled;
    Set<RoleResponse> roles;
    boolean isPublicProfile;
}