package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlbumResponse {
    String id;
    String name;
    String description;
    String avatarUrl;//có gì sửa sau
//    Set<SongResponse> songs;
    //Mapping thêm thồng tin nghệ sĩ (avatar và tên)
}
