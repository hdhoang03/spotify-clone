package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardDataResponse {
    Long totalStreams;
    List<StreamStatResponse> streamData;
    List<GenreStatResponse> genreData;
    List<UserGrowthResponse> userGrowthData;
}
