package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.service.SongService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/album/{albumId}")
    ApiResponse<Page<SongResponse>> getSongsByAlbum(@PathVariable String albumId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<Page<SongResponse>>builder()
                .code(1000)
                .message("All songs from album have been fetched!")
                .result(songService.getSongsByAlbum(albumId, PageRequest.of(page - 1, size)))
                .build();
    }

    @GetMapping("/artist/{artistId}")
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
    ApiResponse<Page<SongResponse>> searchSongsByTitle(@RequestParam String keyword,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<Page<SongResponse>>builder()
                .code(1000)
                .message("Songs...")
                .result(songService.searchSongsByTitle(keyword, PageRequest.of(page - 1, size)))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    ApiResponse<Void> deleteSong(@PathVariable String id){
        songService.deleteSong(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Song has been deleted!")
                .build();
    }

    @GetMapping("/advanced-search")
    ApiResponse<Page<SongResponse>> multipleSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){

        Pageable pageable = PageRequest.of(page - 1, size);
        return ApiResponse.<Page<SongResponse>>builder()
                .code(1000)
                .message("Search results")
                .result(songService.searchSongs(keyword, artist, category, year, pageable))
                .build();
    }
}