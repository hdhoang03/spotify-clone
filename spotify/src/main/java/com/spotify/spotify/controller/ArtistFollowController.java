package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.ArtistFollow;
import com.spotify.spotify.service.ArtistFollowService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArtistFollowController {
    ArtistFollowService artistFollowService;

    @PostMapping("/{userId}/artist/{artistId}")
    ApiResponse<Boolean> toggleFollow(@PathVariable String userId, @PathVariable String artistId){
        boolean isFollowed = artistFollowService.toggleFollow(userId, artistId);
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .message(isFollowed ? "Followed artist successfully" : "Unfollowed artist successfully")
                .result(isFollowed)
                .build();
    }

    @GetMapping("/{userId}/artist")
    ApiResponse<List<ArtistResponse>> getFollowedArtists(@PathVariable String userId){
        return ApiResponse.<List<ArtistResponse>>builder()
                .code(1000)
                .message("Get followed artists successfully.")
                .result(artistFollowService.getFollowedArtists(userId))
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
