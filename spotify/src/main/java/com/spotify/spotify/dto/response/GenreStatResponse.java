package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreStatResponse {
    String name; //Tên thể loại
    Long value; //Số lượng bài hát
    String color; //Biến màu của biểu đồ
}
