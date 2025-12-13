package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.service.AlbumService;
import com.spotify.spotify.service.ArtistService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artist")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArtistController {
    ArtistService artistService;
    AlbumService albumService;

    @PostMapping("/create")
    ApiResponse<ArtistResponse> createArtist(@ModelAttribute ArtistRequest request){
        ArtistResponse response = artistService.createArtist(request);
        return ApiResponse.<ArtistResponse>builder()
                .code(1000)
                .message("Artist has been created!")
                .result(response)
                .build();
    }

    @GetMapping("/all")
    ApiResponse<Page<ArtistResponse>> getAllArtists(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<ArtistResponse>>builder()
                .code(1000)
                .message("All artists are fetched!")
                .result(artistService.getAllArtists(PageRequest.of(page - 1, size, Sort.by("name").ascending())))
                .build();
    }

    @GetMapping("/{artistId}/albums")//Do sai tên biến @PathVariable là albumId
    ApiResponse<List<AlbumResponse>> getAlbumsByArtist(@PathVariable String artistId){
        return ApiResponse.<List<AlbumResponse>>builder()
                .code(1000)
                .message("Albums by artist fetched successfully!")
                .result(albumService.getAlbumsByArtist(artistId))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ArtistResponse> getArtistById(@PathVariable String id){
        ArtistResponse response = artistService.getArtistById(id);
        return ApiResponse.<ArtistResponse>builder()
                .code(1000)
                .message("Artist fetched!")
                .result(response)
                .build();
    }

    @GetMapping("/search")
    ApiResponse<Page<ArtistResponse>> searchArtists(@RequestParam String keyword,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<Page<ArtistResponse>>builder()
                .code(1000)
                .message("")
                .result(artistService.searchArtists(keyword, PageRequest.of(page - 1, size)))
                .build();
    }

    @PutMapping("/update/{id}")
    ApiResponse<ArtistResponse> updateArtist(@PathVariable String id, ArtistRequest request){
        ArtistResponse response = artistService.updateArtist(id, request);
        return ApiResponse.<ArtistResponse>builder()
                .code(1000)
                .message("Artist has been updated!")
                .result(response)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    ApiResponse<Void> deleteArtist(@PathVariable String id){
        artistService.deleteArtist(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Artist has been deleted!")
                .build();
    }
}