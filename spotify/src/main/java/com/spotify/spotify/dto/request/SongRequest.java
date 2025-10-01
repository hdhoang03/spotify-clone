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
    String artist;
    String album;
    String genre;
    MultipartFile coverUrl;
    MultipartFile audioUrl;
}