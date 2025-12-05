package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.UserCreationRequest;
import com.spotify.spotify.dto.request.UserUpdateRequest;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.dto.response.UserResponse;
import com.spotify.spotify.service.ArtistFollowService;
import com.spotify.spotify.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/user")
public class UserController {
    UserService userService;
    ArtistFollowService artistFollowService;

    @PostMapping("/create")
    ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<UserResponse>> getListUser(){
        return ApiResponse.<List<UserResponse>>builder()
                .code(1000)
                .message("All users has been fetched!")
                .result(userService.getUser())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    ApiResponse<Void> deleteUser(@PathVariable String id){
        userService.deleteUser(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("User has been deleted!")
                .build();
    }

    @PutMapping("/update/{id}")
    ApiResponse<UserResponse> updateUser(@PathVariable String id, UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("User has been updated!")
                .result(userService.updateUser(request, id))
                .build();
    }

    @PutMapping("/profile/privacy")
    ApiResponse<UserResponse> togglePrivacy(@RequestParam boolean isPublic){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message(isPublic ? "Profile set to public." : "Profile set to private.")
                .result(userService.togglePrivacy(username, isPublic))
                .build();
    }

    @GetMapping("/artist/me")
    ApiResponse<List<ArtistResponse>> getMyFollowedArtists(){
        return ApiResponse.<List<ArtistResponse>>builder()
                .code(1000)
                .message("My favorite artists")
                .result(artistFollowService.getMyFollowedArtists())
                .build();
    }

    @GetMapping("/follow/{userId}/artist")
    ApiResponse<List<ArtistResponse>> getFollowedArtists(@PathVariable String userId){
        return ApiResponse.<List<ArtistResponse>>builder()
                .code(1000)
                .message("Get followed artists successfully.")
                .result(artistFollowService.getFollowedArtists(userId))
                .build();
    }

}