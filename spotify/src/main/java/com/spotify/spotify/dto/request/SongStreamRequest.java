package com.spotify.spotify.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SongStreamRequest {
    @NotNull(message = "SONG_ID_INVALID")
    String songId;

    @NotNull(message = "DURATION_REQUIRED")
    @Min(value = 30, message = "DURATION_TOO_SHORT")
    Long duration; //optional
}
