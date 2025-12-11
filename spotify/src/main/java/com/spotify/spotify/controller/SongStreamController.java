package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.SongStreamRequest;
import com.spotify.spotify.dto.response.SongStreamResponse;
import com.spotify.spotify.dto.response.StreamStatResponse;
import com.spotify.spotify.dto.response.TopStreamResponse;
import com.spotify.spotify.service.SongStreamService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
}
