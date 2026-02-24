package com.spotify.spotify.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlbumRequest {
    String name;
    String description;
    String artistId;
    LocalDate releaseDate;
    MultipartFile avatarUrl;//có gì chỉnh
}