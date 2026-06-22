package com.spotify.spotify.dto.response;

import com.spotify.spotify.constaint.CategoryType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse implements Serializable {
    String id;
    String name;
    String coverUrl;
    String description;
    CategoryType type;
    Integer songCount;
    Boolean active;
//    List<SongResponse> songs;
}