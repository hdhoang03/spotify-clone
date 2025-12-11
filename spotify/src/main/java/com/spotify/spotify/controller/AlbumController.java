package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.AlbumRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.service.AlbumService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/albums")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AlbumController {
    AlbumService albumService;

    @PostMapping("/create")
    ApiResponse<AlbumResponse> createAlbum(@ModelAttribute AlbumRequest request){
        AlbumResponse response = albumService.createAlbum(request);
        return ApiResponse.<AlbumResponse>builder()
                .code(1000)
                .message("Album has been created!")
                .result(response)
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<AlbumResponse>> getAllAlbum(){
        return ApiResponse.<List<AlbumResponse>>builder()
                .code(1000)
                .message("All albums fetched!")
                .result(albumService.getAllAlbum())
                .build();
    }

    @GetMapping("/{albumId}/songs") //Lỗi @Data trong entity nên dùng @Getter & @Setter
    ApiResponse<List<SongResponse>> getSongsFromAlbum(@PathVariable String albumId){
        return ApiResponse.<List<SongResponse>>builder()
                .code(1000)
                .message("All songs from album have been fetched successfully!")
                .result(albumService.getAllSongsFromAlbum(albumId))
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

    @PutMapping("/update/{id}")//Lỗi set.of vì bất biến, chỉ dùng cho create
    ApiResponse<AlbumResponse> updateAlbum(@PathVariable String id, @ModelAttribute AlbumRequest request){
        AlbumResponse response = albumService.updateAlbum(id, request);
        return ApiResponse.<AlbumResponse>builder()
                .code(1000)
                .message("Album has been updated.")
                .result(response)
                .build();
    }

    @DeleteMapping("/delete/{id}") //Lỗi -- tạo 1 album mới để test sau
    ApiResponse<Void> deleteAlbum(@PathVariable String id){
        albumService.deleteAlbum(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Album has been deleted!")
                .build();
    }

    @GetMapping("/search")
    ApiResponse<List<AlbumResponse>> searchAlbum(@RequestParam String keyword){
        return ApiResponse.<List<AlbumResponse>>builder()
                .code(1000)
                .message("Result")
                .result(albumService.searchAlbum(keyword))
                .build();
    }

    //Add song to album
}
