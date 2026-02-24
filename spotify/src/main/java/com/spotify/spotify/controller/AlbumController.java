package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.AlbumRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.service.AlbumService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @PostMapping("/{albumId}/songs") //chưa test
    ApiResponse<String> addSongsToAlbum(@PathVariable String albumId, @RequestBody List<String> songIds){
        albumService.addSongsToAlbum(albumId, songIds);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Songs added to album successfully")
                .build();
    }

    @DeleteMapping("/{albumId}/songs/{songId}")
    ApiResponse<Void> removeSongFromAlbum(@PathVariable String albumId,
                                          @PathVariable String songId){
        albumService.removeSongFromAlbum(albumId, songId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Song has been removed from album")
                .build();
    }

    @GetMapping("/all")
    ApiResponse<Page<AlbumResponse>> getAllAlbum(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<Page<AlbumResponse>>builder()
                .code(1000)
                .message("All albums fetched!")
                .result(albumService.getAllAlbum(PageRequest.of(page - 1, size)))
                .build();
    }

    @GetMapping("/{albumId}/songs") //Lỗi @Data trong entity nên dùng @Getter & @Setter
    ApiResponse<Page<SongResponse>> getSongsFromAlbum(@PathVariable String albumId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<Page<SongResponse>>builder()
                .code(1000)
                .message("All songs from album have been fetched successfully!")
                .result(albumService.getAllSongsFromAlbum(albumId, PageRequest.of(page - 1, size)))
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

    @GetMapping("/list")
    ApiResponse<Page<AlbumResponse>> searchAlbum(@RequestParam(defaultValue = "", required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "false") boolean isDeleted){
        return ApiResponse.<Page<AlbumResponse>>builder()
                .code(1000)
                .result(albumService.searchAlbum(keyword, isDeleted, PageRequest.of(page - 1, size)))
                .build();
    }
}