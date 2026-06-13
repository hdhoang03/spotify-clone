package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TopLikeSongResponse implements Serializable {
    String songTitle;
    String songId;
    String artistId;
    String artistName;
    String coverUrl;
    String audioUrl;
    Long streamCount;
    Double duration;
}
