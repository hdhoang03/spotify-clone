package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.response.ArtistFollowResponse;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.ArtistFollow;
import com.spotify.spotify.service.ArtistFollowService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArtistFollowController {
    ArtistFollowService artistFollowService;

    @PostMapping("/{artistId}")
    ApiResponse<Boolean> toggleFollow(@PathVariable String artistId){
        boolean isFollowed = artistFollowService.toggleFollow(artistId);
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .message(isFollowed ? "Followed artist successfully" : "Unfollowed artist successfully")
                .result(isFollowed)
                .build();
    }

    @GetMapping("/{userId}/artists")
    ApiResponse<Page<ArtistFollowResponse>> getFollowedArtists(@PathVariable String userId,
                                                                        @RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<Page<ArtistFollowResponse>>builder()
                .code(1000)
                .message("Get followed artists successfully.")
                .result(artistFollowService.getFollowedArtists(userId, PageRequest.of(page - 1, size)))
                .build();
    }

    @GetMapping("/artist/{artistId}/count")
    ApiResponse<Long> getFollowerCount(@PathVariable String artistId){
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Get follower count successfully.")
                .result(artistFollowService.getFollowerCount(artistId))
                .build();
    }
}
