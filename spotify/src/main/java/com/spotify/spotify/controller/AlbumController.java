package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.AlbumRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.service.AlbumService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/albums")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AlbumController {
    AlbumService albumService;

    @PostMapping("/create")
    ApiResponse<AlbumResponse> createAlbum(AlbumRequest request){
        AlbumResponse response = albumService.createAlbum(request);
        return ApiResponse.<AlbumResponse>builder()
                .code(1000)
                .message("Album has been created!")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<AlbumResponse> getAlbumById(@PathVariable String id){
        AlbumResponse response = albumService.getAlbumById(id);
        return ApiResponse.<AlbumResponse>builder()
                .code(1000)
                .message("Albums is fetched!")
                .result(response)
                .build();
    }

    @PutMapping("/update/{id}")
    ApiResponse<AlbumResponse> updateAlbum(@PathVariable String id, AlbumRequest request){
        AlbumResponse response = albumService.updateAlbum(id, request);
        return ApiResponse.<AlbumResponse>builder()
                .code(1000)
                .message("Album has been updated.")
                .result(response)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    ApiResponse<Void> deleteAlbum(@PathVariable String id){
        albumService.deleteAlbum(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Album has been deleted!")
                .build();
    }
}
