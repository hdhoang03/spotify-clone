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
public class TopStreamResponse implements Serializable {
    String songTitle;
    String songId;
    String artistId;
    String artistName;
    String coverUrl;
    String audioUrl;
    Long count;
    Double duration;
}
