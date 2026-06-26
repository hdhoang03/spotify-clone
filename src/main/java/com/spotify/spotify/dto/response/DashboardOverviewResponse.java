package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardOverviewResponse implements Serializable {
    long totalUsers;
    long totalSongs;
    long totalArtists;
    long totalAlbums;
    List<TopStreamResponse> topSongs;
}
