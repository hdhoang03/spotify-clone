package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.service.PlaylistService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlist")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaylistController {
    PlaylistService playlistService;

    @GetMapping("/{playlistId}")
    ApiResponse<PlaylistResponse> getPlaylist(@PathVariable String playlistId){
        return ApiResponse.<PlaylistResponse>builder()
                .code(1000)
//                .message()
                .result(playlistService.getPlaylist(playlistId))
                .build();
    }

    @PostMapping("/create")
    ApiResponse<PlaylistResponse> createPlaylist(@ModelAttribute PlaylistRequest request){
        return ApiResponse.<PlaylistResponse>builder()
                .code(1000)
                .result(playlistService.createPlaylist(request))
                .message("Create playlist successfully!")
                .build();
    }

    @PutMapping("/update")
    ApiResponse<PlaylistResponse> updatePlaylist(@ModelAttribute PlaylistUpdateRequest request, @PathVariable String playlistId){
        return ApiResponse.<PlaylistResponse>builder()
                .code(1000)
                .result(playlistService.updatePlaylist(playlistId, request))
                .message("Update playlist successfully!")
                .build();
    }

    @DeleteMapping("/delete")
    ApiResponse<Void> deletePlaylist(@PathVariable String playlistId){
        playlistService.deletePlaylist(playlistId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Delete playlist successfully!")
                .build();
    }
}
