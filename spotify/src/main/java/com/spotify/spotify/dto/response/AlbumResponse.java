package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlbumResponse implements Serializable {
    String id;
    String name;
    String description;
    String albumUrl;
    Integer songCount;
    String artistName;
    LocalDate releaseDate;
//    Set<SongResponse> songs;
    //Mapping thêm thồng tin nghệ sĩ (avatar và tên)
}
