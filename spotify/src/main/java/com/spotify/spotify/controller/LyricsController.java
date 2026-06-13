package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.response.LyricResponse;
import com.spotify.spotify.service.LyricsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/lyrics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LyricsController {
    LyricsService lyricsService;

    @GetMapping("/{songId}/get")
    ApiResponse<LyricResponse> getSongLyrics(@PathVariable String songId) {
        return ApiResponse.<LyricResponse>builder()
                .code(1000)
                .message("Success")
                .result(lyricsService.getLyricsBySongId(songId))
                .build();
    }

    @PostMapping("/{songId}")
    ApiResponse<LyricResponse> saveSongLyrics(@PathVariable String songId,
                                                @RequestBody LyricResponse request) {
        return ApiResponse.<LyricResponse>builder()
                .code(1000)
                .message("Success")
                .result(lyricsService.saveOrUpdateLyrics(songId, request))
                .build();
    }
}
