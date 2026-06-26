package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ArtistResponse implements Serializable {
    String id;
    String name;
    String description;
    String avatarUrl;
    Boolean isFollowed;
    Integer followerCount;
    Integer songCount;
    String country;
    Map<String, String> socialAccounts;
}