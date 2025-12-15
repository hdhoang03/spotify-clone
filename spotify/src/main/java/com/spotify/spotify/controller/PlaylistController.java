package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.service.PlaylistService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlist")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaylistController {
    PlaylistService playlistService;

    @PostMapping("/{playlistId}/add/{songId}")
    ApiResponse<Void> addSongToPlaylist(@PathVariable String playlistId, @PathVariable String songId){
        playlistService.addSongToPlaylist(playlistId, songId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Song added to this playlist!")
                .build();
    }

    @DeleteMapping("/{playlistId}/remove/{songId}")
    ApiResponse<Void> removeSongFromPlaylist(@PathVariable String playlistId, @PathVariable String songId){
        playlistService.removeSongFromPlaylist(playlistId, songId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Song has been removed from this playlist!")
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<Page<PlaylistResponse>> getUserPublicPlaylist(@PathVariable String userId,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<PlaylistResponse>>builder()
                .code(1000)
                .message("Playlist fetched from this user!")
                .result(playlistService.getUserPublicPlaylists(userId, PageRequest.of(page - 1, size)))
                .build();
    }

    @GetMapping("/my")
    ApiResponse<Page<PlaylistResponse>> getMyPlaylists(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<PlaylistResponse>>builder()
                .code(1000)
                .message("My playlist fetched!")
                .result(playlistService.getMyPlaylists(PageRequest.of(page - 1, size)))
                .build();
    }

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

    @PutMapping("/update/{playlistId}")
    ApiResponse<PlaylistResponse> updatePlaylist(@ModelAttribute PlaylistUpdateRequest request, @PathVariable String playlistId){
        return ApiResponse.<PlaylistResponse>builder()
                .code(1000)
                .result(playlistService.updatePlaylist(playlistId, request))
                .message("Update playlist successfully!")
                .build();
    }

    @DeleteMapping("/delete/{playlistId}")
    ApiResponse<Void> deletePlaylist(@PathVariable String playlistId){
        playlistService.deletePlaylist(playlistId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Delete playlist successfully!")
                .build();
    }
}
