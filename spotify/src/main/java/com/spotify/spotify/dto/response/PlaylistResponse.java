package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder //LƯU Ý: không tự mapping các trường có tên giống nhau, phải mapping thủ công
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaylistResponse {
    String id;
    String name;
    String description;
    String coverUrl;
    Boolean isPublic;
    LocalDateTime createdAt;
    UserSummaryResponse user;
    List<SongResponse> songs;
    Integer songCount;
}
