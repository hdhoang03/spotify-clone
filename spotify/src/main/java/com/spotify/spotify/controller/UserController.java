package com.spotify.spotify.controller;

import com.cloudinary.Api;
import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.SupportRequest;
import com.spotify.spotify.dto.request.UserCreationRequest;
import com.spotify.spotify.dto.request.UserProfileUpdateRequest;
import com.spotify.spotify.dto.request.UserUpdateRequest;
import com.spotify.spotify.dto.response.*;
import com.spotify.spotify.service.ArtistFollowService;
import com.spotify.spotify.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    ApiResponse<Page<UserResponse>> searchUser(@RequestParam(defaultValue = "", required = false) String keyword,
                                               @RequestParam(defaultValue = "", required = false) String currentUserId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.<Page<UserResponse>>builder()
                .code(1000)
                .message("Users have been fetched!")
                .result(userService.searchUser(keyword, currentUserId, pageable))
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
    ApiResponse<Boolean> togglePrivacy(){
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .result(userService.togglePrivacy())
                .message("Privacy status updated successfully!")
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

    @GetMapping("/{userId}/profile")
    ApiResponse<UserProfileResponse> getUserResponse(@PathVariable String userId){
        return ApiResponse.<UserProfileResponse>builder()
                .code(1000)
                .message("User's profile has been fetched!")
                .result(userService.getUserProfile(userId))
                .build();
    }

    @PostMapping("/{userId}/follow")
    ApiResponse<Boolean> toggleFollowUser(@PathVariable String userId){
        boolean isFollowed = userService.toggleFollowUser(userId);
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .message(isFollowed ? "Followed user successfully" : "Unfollowed user successfully")
                .result(isFollowed)
                .build();
    }

    @GetMapping("/{userId}/following-users")
    ApiResponse<Page<UserSummaryResponse>> getFollowingUsers(@PathVariable String userId,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<UserSummaryResponse>>builder()
                .code(1000)
                .message("Get following users successfully.")
                .result(userService.getFollowingUsers(userId, PageRequest.of(page - 1, size)))
                .build();
    }

    @GetMapping("/{userId}/followers")
    ApiResponse<Page<UserSummaryResponse>> getFollowers(@PathVariable String userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<UserSummaryResponse>>builder()
                .code(1000)
                .message("Get follower successfully.")
                .result(userService.getFollowers(userId, PageRequest.of(page - 1, size)))
                .build();
    }

    @PutMapping("/profile/update")
    ApiResponse<UserProfileResponse> updateMyProfile(@ModelAttribute UserProfileUpdateRequest request){
        return ApiResponse.<UserProfileResponse>builder()
                .code(1000)
                .message("Profile upload successfully!")
                .result(userService.updateMyProfile(request))
                .build();
    }

    @PostMapping("/support")
    ApiResponse<Void> sendSupportRequest(@RequestBody SupportRequest request){
        userService.sendSupportEmail(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Support request sent successfully!")
                .build();
    }

    @GetMapping("/blocked")
    ApiResponse<Page<UserSummaryResponse>> getBlockedUser(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<UserSummaryResponse>>builder()
                .code(1000)
                .message("Fetched blocked user successfully!")
                .result(userService.getBlockedUser(PageRequest.of(page - 1, size)))
                .build();
    }

    @PostMapping("/{userId}/block")
    ApiResponse<Boolean> toggleBlockUser(@PathVariable String userId){
        boolean isBlocked = userService.toggleBlockUser(userId);
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .message(isBlocked ? "Blocked user successfully" : "Unblocked user successfully")
                .build();
    }
}