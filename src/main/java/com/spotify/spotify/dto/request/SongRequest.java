package com.spotify.spotify.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SongRequest {
    String title;
    String artistId;
    String albumId;
    String categoryId;
    String category;
    MultipartFile coverUrl;
    MultipartFile audioUrl;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate releaseDate;
    Set<String> featuredArtistIds;
}