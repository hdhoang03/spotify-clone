package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchResponse {
    List<ArtistResponse> artists;
    List<SearchSongResponse> songs;
    List<AlbumResponse> albums;
    List<CategoryResponse> categories;
}
