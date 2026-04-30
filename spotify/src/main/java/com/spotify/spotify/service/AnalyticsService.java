package com.spotify.spotify.service;

import com.spotify.spotify.dto.response.DashboardDataResponse;
import com.spotify.spotify.dto.response.GenreStatResponse;
import com.spotify.spotify.dto.response.StreamStatResponse;
import com.spotify.spotify.dto.response.UserGrowthResponse;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.repository.SongStreamRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsService {
    SongRepository songRepository;
    SongStreamRepository songStreamRepository;
    UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDataResponse getDashBoardData(String timeRange, int year, int month){
        Long totalStreams = songStreamRepository.countStreamsByMonth(year, month);
        if (totalStreams == null) totalStreams = 0L;

        List<GenreStatResponse> genreData = songRepository.countSongsByCategory();
        String[] colors = {"#3b82f6", "#22c55e", "#eab308", "#ec4899", "#a855f7", "#ef4444", "#f97316"};
        for (int i = 0; i < genreData.size(); i++){
            genreData.get(i).setColor(colors[i % colors.length]);
        }

        List<UserGrowthResponse> userGrowthData = userRepository.countUserGrowthByYear(year);

        LocalDateTime start, end;
        LocalDate today = LocalDate.now();

        if ("year".equalsIgnoreCase(timeRange)){
            start = LocalDateTime.of(year, 1, 1, 0, 0);
            end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        } else if ("month".equalsIgnoreCase(timeRange)){
            YearMonth ym = YearMonth.of(year, month);
            start = ym.atDay(1).atStartOfDay();
            end = ym.atEndOfMonth().atTime(23, 59, 59);
        } else {
            start = today.minusDays(6).atStartOfDay();
            end = today.atTime(23, 59, 59);
        }

        List<StreamStatResponse> streamData = songStreamRepository.getOverallStreamStats(start, end);

        return DashboardDataResponse.builder()
                .totalStreams(totalStreams)
                .genreData(genreData)
                .userGrowthData(userGrowthData)
                .streamData(streamData)
                .build();
    }
}
