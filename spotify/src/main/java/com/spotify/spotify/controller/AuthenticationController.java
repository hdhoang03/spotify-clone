package com.spotify.spotify.controller;

import com.cloudinary.Api;
import com.nimbusds.jose.JOSEException;
import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.*;
import com.spotify.spotify.dto.response.AuthenticationResponse;
import com.spotify.spotify.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/register")
    ApiResponse<Void> register(@RequestBody UserCreationRequest request){
        authenticationService.register(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("OTP has been sent to your email. Please verify!")
                .build();
    }

    //thêm resend otp

    @PostMapping("/verify")
    ApiResponse<AuthenticationResponse> verify(@RequestBody VerifyOtpRequest request){
        var auth = authenticationService.verifyAndCreateUser(request.getEmail(), request.getOtpCode());
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .result(auth)
                .build();
    }

    @PostMapping("/change-password")
    ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request){
        authenticationService.changePassword(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Password changed successfully!")
                .build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request){
        authenticationService.sendForgotPasswordOtp(request.getEmail());
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("OTP sent to your email.")
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request){
        authenticationService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Password reset successfully. You can login now.")
                .build();
    }

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticationResponse(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws JOSEException, ParseException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Logout successful!")
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws JOSEException, ParseException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .result(result)
                .build();
    }
}
