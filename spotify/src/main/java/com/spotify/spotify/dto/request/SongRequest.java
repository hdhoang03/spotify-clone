package com.spotify.spotify.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SongRequest {
    String title;
    String artistId;
    String albumId;
    String genre;//Category
    MultipartFile coverUrl;
    MultipartFile audioUrl;
}