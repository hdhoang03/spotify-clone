package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaylistSongResponse implements Serializable {
    String id;
    String title;
    String artist;
    String artistId;
    String albumName;
    String coverUrl;
    String audioUrl;
    Double duration;
    LocalDate addedAt;
}
