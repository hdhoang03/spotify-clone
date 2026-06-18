package com.spotify.spotify.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArtistRequest {
    String name;
    String description;
    MultipartFile avatarUrl;
    String country;
    LocalDate debutDate;
    Map<String, String> socialAccounts;
}