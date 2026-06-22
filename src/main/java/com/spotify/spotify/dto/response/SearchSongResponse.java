package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchSongResponse implements Serializable {
    String id;
    String title;
    String coverUrl;
    String audioUrl;
    String artistName;
    String artistId;
    List<FeaturedArtistInfo> featuredArtists; //gra 0706
}
