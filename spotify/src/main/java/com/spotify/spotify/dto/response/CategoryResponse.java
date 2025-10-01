package com.spotify.spotify.dto.response;

import com.spotify.spotify.entity.Song;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    String name;
    String description;
    List<SongResponse> songs;
}