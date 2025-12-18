package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.UserCreationRequest;
import com.spotify.spotify.dto.request.UserUpdateRequest;
import com.spotify.spotify.dto.response.ArtistFollowResponse;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.dto.response.UserResponse;
import com.spotify.spotify.service.ArtistFollowService;
import com.spotify.spotify.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Setter
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
    ApiResponse<Void> togglePrivacy(@RequestParam Boolean isPublic){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.togglePrivacy(username, isPublic);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message(isPublic ? "Profile set to public." : "Profile set to private.")
                .build();
    }

    @GetMapping("/artist/me") //cá nhân
    ApiResponse<Page<ArtistResponse>> getMyFollowedArtists(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<Page<ArtistResponse>>builder()
                .code(1000)
                .message("My favorite artists")
                .result(artistFollowService.getMyFollowedArtists(PageRequest.of(page -1, size)))
                .build();
    }

    @GetMapping("/follow/{userId}/artist")//cá nhân/ người khác
    ApiResponse<Page<ArtistFollowResponse>> getFollowedArtists(@PathVariable String userId,
                                                               @RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<ArtistFollowResponse>>builder()
                .code(1000)
                .message("Get followed artists successfully.")
                .result(artistFollowService.getFollowedArtists(userId, PageRequest.of(page - 1, size)))
                .build();
    }

}