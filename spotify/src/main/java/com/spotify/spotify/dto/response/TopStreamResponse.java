package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopStreamResponse {
    String songTitle;
    String songId;
    String artistName;
    String coverUrl;
    String audioUrl;
    Long count;
    Double duration;
}
