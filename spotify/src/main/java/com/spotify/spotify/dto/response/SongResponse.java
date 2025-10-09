package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SongResponse {
    String id;
    String title;
    String artist;
    String album;
    String genre;//Category
    String coverUrl;
    String audioUrl;
    String uploadedBy;
    LocalDateTime createAt;
}