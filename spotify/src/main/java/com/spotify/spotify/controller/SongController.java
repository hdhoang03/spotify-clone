package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.service.SongService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/song")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SongController {
    SongService songService;

    @PostMapping("/create") //Local
    ApiResponse<SongResponse> createSong(@ModelAttribute SongRequest request){
        SongResponse response = songService.createSong(request);
        return ApiResponse.<SongResponse>builder()
                .code(1000)
                .message("Song has been created!")
                .result(response)
                .build();
    }

    @PostMapping("/create2") //Cloudinary
    ApiResponse<SongResponse> createSong2(@ModelAttribute SongRequest request){
        return ApiResponse.<SongResponse>builder()
                .code(1000)
                .message("Song has been uploaded!")
                .result(songService.createSong(request))
                .build();
    }

    @PutMapping("/update/{id}")
    ApiResponse<SongResponse> updateSong(@PathVariable String id, @ModelAttribute SongRequest request){//Có file thì dùng @modelAttribute
        SongResponse response = songService.updateSong(id, request);
        return ApiResponse.<SongResponse>builder()
                .code(1000)
                .message("Song has been updated!")
                .result(response)
                .build();
    }

    @GetMapping("/allSongs")
    ApiResponse<List<SongResponse>> getAllSongs(){
        return ApiResponse.<List<SongResponse>>builder()
                .code(1000)
                .message("All songs are fetched!")
                .result(songService.getAllSongs())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<SongResponse> getSong(@PathVariable String id){
        SongResponse response = songService.getSong(id);
        return ApiResponse.<SongResponse>builder()
                .code(1000)
                .message("Song detail fetched!")
                .result(response)
                .build();
    }

    @GetMapping("/{albumId}")
    ApiResponse<List<SongResponse>> getSongsByAlbum(@PathVariable String albumId){
        return ApiResponse.<List<SongResponse>>builder()
                .code(1000)
                .message("All songs from album have been fetched!")
                .result(songService.getSongsByAlbum(albumId))
                .build();
    }

    @GetMapping("/{artistId}")
    ApiResponse<List<SongResponse>> getSongsByArtist(@PathVariable String artistId){
        return ApiResponse.<List<SongResponse>>builder()
                .code(1000)
                .message("All songs from artist have been fetched!")
                .result(songService.getSongByArtist(artistId))
                .build();
    }

    @GetMapping("/newSongs")
    ApiResponse<List<SongResponse>> getAllSongsByDay(){
        return ApiResponse.<List<SongResponse>>builder()
                .code(1000)
                .message("Newest songs have been fetched!")
                .result(songService.getAllSongsByDay())
                .build();
    }

    @GetMapping("/search")
    ApiResponse<List<SongResponse>> searchSongsByTitle(@PathVariable String keyword){
        return ApiResponse.<List<SongResponse>>builder()
                .code(1000)
                .message("Songs...")
                .result(songService.searchSongsByTitle(keyword))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteSong(@PathVariable String id){
        songService.deleteSong(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Song has been deleted!")
                .build();
    }
}