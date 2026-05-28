package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.SongStreamRequest;
import com.spotify.spotify.dto.response.*;
import com.spotify.spotify.service.SongStreamService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SongStreamController {
    SongStreamService songStreamService;

    @PostMapping("/play/{songId}")
    ApiResponse<Void> increasePlayCount(@PathVariable String songId){
        songStreamService.increasePlayCount(songId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Play count incremented")
                .build();
    }

    @PostMapping("/create")
    ApiResponse<SongStreamResponse> createStream(@RequestBody @Valid SongStreamRequest request){
        return ApiResponse.<SongStreamResponse>builder()
                .code(1000)
                .message("Stream created successfully.")
                .result(songStreamService.createStream(request))
                .build();
    }

    @GetMapping("/count/{songId}")
    ApiResponse<Long> countSongStream(@PathVariable String songId){
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Total streams")
                .result(songStreamService.countSongStream(songId))
                .build();
    }

    @GetMapping("/myStream")
    ApiResponse<Page<SongStreamResponse>> getMyStreams(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size

    ){
        return ApiResponse.<Page<SongStreamResponse>>builder()
                .code(1000)
                .message("All my streams")
                .result(songStreamService.getMyStreams(PageRequest.of(page -1, size)))
                .build();
    }

    @GetMapping("/check/{songId}")
    ApiResponse<Boolean> hasUserStreamedSong(@PathVariable String songId){
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .message("Ok")
                .result(songStreamService.hasUserStreamedSong(songId))
                .build();
    }

    @GetMapping("/range")
    ApiResponse<List<StreamStatResponse>> getStreamStats(@RequestParam String songId,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){
        return ApiResponse.<List<StreamStatResponse>>builder()
                .code(1000)
                .result(songStreamService.getStreamStats(songId, start, end))
                .build();
    }

    @GetMapping("/top")
    ApiResponse<List<TopStreamResponse>> getTopStreamSongs(){
        return ApiResponse.<List<TopStreamResponse>>builder()
                .code(1000)
                .message("Top songs")
                .result(songStreamService.getTopStreamSongs())
                .build();
    }

    @GetMapping("/my-tracks")
    ApiResponse<List<TopLikeSongResponse>> getMyTopTracksOfThisMonth(@RequestParam(required = false) Integer month,
                                                                     @RequestParam(required = false) Integer year){

        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();

        return ApiResponse.<List<TopLikeSongResponse>>builder()
                .code(1000)
                .message("Hehe")
                .result(songStreamService.getMyTopTracksOfThisMonth(month, year))
                .build();
    }

    @GetMapping("/my-artists")
    ApiResponse<List<ArtistResponse>> getMyTopArtistsOfMonth(@RequestParam(required = false) Integer month,
                                                             @RequestParam(required = false) Integer year){

        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();

        return ApiResponse.<List<ArtistResponse>>builder()
                .code(1000)
                .message("hehe")
                .result(songStreamService.getMyTopArtistsOfMonth(month, year))
                .build();
    }

    @GetMapping("/{songId}/get-valid-stream-count")
    ApiResponse<Long> getValidStreamCount(@PathVariable String songId){
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("All valid streams")
                .result(songStreamService.getValidStreamCount(songId))
                .build();
    }
}
