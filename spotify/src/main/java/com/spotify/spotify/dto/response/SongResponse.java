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
    String albumId;
    String albumName;
    String category;
    String coverUrl;
    String audioUrl;
    String uploadedBy;
    Double duration; //mới thêm
    LocalDateTime createdAt;
}