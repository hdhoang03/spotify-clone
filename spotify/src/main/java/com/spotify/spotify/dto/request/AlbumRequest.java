package com.spotify.spotify.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlbumRequest {
    String name;
    String description;
    String artistId;
    MultipartFile avatarUrl;//có gì chỉnh
}