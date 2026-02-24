package com.spotify.spotify.controller;

import com.cloudinary.Api;
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
import org.springframework.data.domain.Pageable;
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

    @PutMapping("/toggle-status/{userId}")
    ApiResponse<Void> toggleUserStatus(@PathVariable String userId){
        userService.toggleUserStatus(userId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("User status has been updated!")
                .build();
    }

    @GetMapping("/my")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .result(userService.getMyInfo())
                .build();
    }

    @GetMapping("/list")
    ApiResponse<Page<UserResponse>> getUser(@RequestParam(defaultValue = "", required = false) String keyword,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<UserResponse>>builder()
                .code(1000)
                .message("Users have been fetched!")
                .result(userService.searchUser(keyword, PageRequest.of(page - 1, size)))
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