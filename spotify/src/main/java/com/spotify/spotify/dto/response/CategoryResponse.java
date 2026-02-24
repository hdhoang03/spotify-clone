package com.spotify.spotify.dto.response;

import com.spotify.spotify.constaint.CategoryType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    String id;
    String name;
    String coverUrl;
    String description;
    String backgroundColor;
    CategoryType type;
    Integer songCount;
    Boolean active;
//    List<SongResponse> songs;
}