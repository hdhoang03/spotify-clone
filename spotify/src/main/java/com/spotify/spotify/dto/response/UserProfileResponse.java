package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    String id;
    String username;
    String name;
    String email;
    String avatarUrl;
    String bio;
    Long followerCount;
    Long followingCount;
    Long playlistCount;
    Long followingArtistCount;
    Boolean isFollowedByMe;
    boolean isPublicProfile;
}
