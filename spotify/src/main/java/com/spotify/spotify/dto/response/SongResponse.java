package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SongResponse {
    String id;
    String title;
    String artistId;
    String artist;
    String albumId;
    String albumName;
    String categoryId;
    String category;
    String coverUrl;
    String audioUrl;
    String uploadedBy;
    Double duration;
    LocalDateTime createdAt;
    LocalDate releaseDate;
    Long playCount;
    Long streamCount;
//    List<ArtistInfo> featureArtists;
//    public record ArtistInfo(String id, String name) {}
}