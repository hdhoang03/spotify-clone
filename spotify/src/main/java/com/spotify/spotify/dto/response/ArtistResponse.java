package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtistResponse {
    String id;
    String name;
    String description;
    String avatarUrl;
//    List<SongResponse> songs;
}