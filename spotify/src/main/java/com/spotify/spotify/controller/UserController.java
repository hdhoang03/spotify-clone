package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.UserCreationRequest;
import com.spotify.spotify.dto.request.UserUpdateRequest;
import com.spotify.spotify.dto.response.UserResponse;
import com.spotify.spotify.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/user")
public class UserController {
    UserService userService;

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
}