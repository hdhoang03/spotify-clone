package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.response.LikeSongResponse;
import com.spotify.spotify.dto.response.TopLikeSongResponse;
import com.spotify.spotify.service.LikeSongService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/like")
public class LikeSongController {
    LikeSongService likeSongService;

    @GetMapping("/check/{songId}")
    ApiResponse<Boolean> hasLiked(@PathVariable String songId){
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .result(likeSongService.hasLiked(songId))
                .build();
    }

    @GetMapping("/count/{songId}")
    ApiResponse<Long> countSongLikes(@PathVariable String songId){
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("All likes from this song")
                .result(likeSongService.countSongLikes(songId))
                .build();
    }

    @GetMapping("/my")
    ApiResponse<Page<LikeSongResponse>> getMyLikedSongs(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<Page<LikeSongResponse>>builder()
                .code(1000)
                .message("My liked songs")
                .result(likeSongService.getMyLikedSongs(PageRequest.of(page - 1, size)))
                .build();
    }

//    @PostMapping("/{songId}")
//    ApiResponse<LikeSongResponse> likeSong(@PathVariable String songId){
//        return ApiResponse.<LikeSongResponse>builder()
//                .code(1000)
//                .message("Liked this song")
//                .result(likeSongService.likeSong(songId))
//                .build();
//    }

    @PostMapping("/{songId}")
    ApiResponse<Void> likeSong(@PathVariable String songId){
        likeSongService.likeSong(songId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Liked this song")
                .build();
    }

    @PostMapping("/{songId}/unlike")
    ApiResponse<Void> unlikeSong(@PathVariable String songId){
        likeSongService.unlikeSong(songId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Unliked this song")
                .build();
    }

    @GetMapping("/top")
    ApiResponse<Page<TopLikeSongResponse>> getTopLikedSongs(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<TopLikeSongResponse>>builder()
                .code(1000)
                .message("Top liked songs")
                .result(likeSongService.getTopLikedSongs(PageRequest.of(page - 1, size)))
                .build();
    }
}