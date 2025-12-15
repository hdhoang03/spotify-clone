package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchSongResponse {
    String id;
    String title;
    String coverUrl;
    String audioUrl;
    String artistName;
    String artistId;
}
