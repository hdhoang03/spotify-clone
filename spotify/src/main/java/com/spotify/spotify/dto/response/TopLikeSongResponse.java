package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TopLikeSongResponse {
    String songTitle;
    String songId;
    String artistName;
    String coverUrl;
    Long likeCount;
    Double duration;
}
