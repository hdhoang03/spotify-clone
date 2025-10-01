package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.service.ArtistService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/artist")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArtistController {
    ArtistService artistService;

    @PostMapping("/create")
    ApiResponse<ArtistResponse> createArtist(ArtistRequest request){
        ArtistResponse response = artistService.createArtist(request);
        return ApiResponse.<ArtistResponse>builder()
                .code(1000)
                .message("Artist has been created!")
                .result(response)
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