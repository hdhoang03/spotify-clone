package com.spotify.spotify.service;

import com.spotify.spotify.dto.response.*;
import com.spotify.spotify.repository.AlbumRepository;
import com.spotify.spotify.repository.ArtistRepository;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.repository.SongStreamRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AnalyticsService {
    SongRepository songRepository;
    SongStreamRepository songStreamRepository;
    UserRepository userRepository;
    AlbumRepository albumRepository;
    ArtistRepository artistRepository;

    // Gộp 5 API thành 1, cache 5 phút
    @Cacheable(value = "admin_dashboard_overview")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardOverviewResponse getDashboardOverview() {
        try {
            long totalUsers = userRepository.count();
            long totalSongs = songRepository.countByDeletedFalse();
            long totalArtists = artistRepository.countByDeletedFalse();
            long totalAlbums = albumRepository.count();

            List<TopStreamResponse> topSongs = songStreamRepository.findTopStreamSongs();

            return DashboardOverviewResponse.builder()
                    .totalUsers(totalUsers)
                    .totalSongs(totalSongs)
                    .totalArtists(totalArtists)
                    .totalAlbums(totalAlbums)
                    .topSongs(topSongs)
                    .build();
        } catch (Exception e) {
            log.error("[AnalyticsService] getDashboardOverview lỗi: {}", e.getMessage(), e);
            // Dùng new ArrayList<>() thay vì List.of() — vì List.of() tạo ImmutableCollections$EmptyList
            // — class nội bộ JDK không Jackson deserialize được khi activateDefaultTyping được bật
            return DashboardOverviewResponse.builder()
                    .totalUsers(0)
                    .totalSongs(0)
                    .totalArtists(0)
                    .totalAlbums(0)
                    .topSongs(new ArrayList<>())
                    .build();
        }
    }

    @Cacheable(value = "admin_dashboard", key = "#timeRange + '_' + #year + '_' + #month")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDataResponse getDashBoardData(String timeRange, int year, int month){
        try {
            Long totalStreams = songStreamRepository.countStreamsByMonth(year, month);
            if (totalStreams == null) totalStreams = 0L;

            List<GenreStatResponse> genreData = songRepository.countSongsByCategory();
            String[] colors = {"#3b82f6", "#22c55e", "#eab308", "#ec4899", "#a855f7", "#ef4444", "#f97316"};
            for (int i = 0; i < genreData.size(); i++) {
                genreData.get(i).setColor(colors[i % colors.length]);
            }

            List<UserGrowthResponse> userGrowthData = userRepository.countUserGrowthByYear(year);

            LocalDateTime start, end;
            LocalDate today = LocalDate.now();

            if ("year".equalsIgnoreCase(timeRange)) {
                start = LocalDateTime.of(year, 1, 1, 0, 0);
                end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
            } else if ("month".equalsIgnoreCase(timeRange)) {
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

        } catch (Exception e) {
            log.error("[AnalyticsService] getDashBoardData lỗi (timeRange={}, year={}, month={}): {}",
                    timeRange, year, month, e.getMessage(), e);
            // Dùng new ArrayList<>() thay vì List.of() — cùng lý do: ImmutableCollections$EmptyList
            return DashboardDataResponse.builder()
                    .totalStreams(0L)
                    .genreData(new ArrayList<>())
                    .userGrowthData(new ArrayList<>())
                    .streamData(new ArrayList<>())
                    .build();
        }
    }
}

