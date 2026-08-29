package com.spotify.spotify.controller;

import com.nimbusds.jose.JOSEException;
import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.*;
import com.spotify.spotify.dto.response.AuthenticationResponse;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.service.AuthenticationService;
import com.spotify.spotify.service.RateLimitService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;
    RateLimitService rateLimitService;

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Tạo httpOnly cookie chứa JWT. SameSite=Lax cho phép redirect sau OAuth. */
    private ResponseCookie buildAccessCookie(String token, Duration maxAge) {
        return ResponseCookie.from("access_token", token)
                .httpOnly(true)          // JavaScript KHÔNG đọc được
                .secure(true)            // BẮT BUỘC true khi dùng SameSite=None
                .sameSite("None")        // Cho phép gửi cookie cross-domain (FE vercel -> BE onrender)
                .path("/")               // Gửi kèm mọi request
                .maxAge(maxAge)
                .build();
    }

    /** Tạo cookie rỗng để xóa cookie phía browser. */
    private ResponseCookie buildClearCookie() {
        return ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    private void setTokenCookie(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildAccessCookie(token, Duration.ofHours(24)).toString());
    }

    private void clearTokenCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildClearCookie().toString());
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> "access_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // ── Endpoints ──────────────────────────────────────────────────────────────

    /**
     * Google OAuth2 callback – set cookie sau khi xác thực thành công.
     */
    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthentication(
            @RequestParam("code") String code,
            @RequestParam(value = "redirectUri", required = false) String redirectUri,
            HttpServletResponse response) {

        var result = authenticationService.outboundAuthenticate(code, redirectUri);
        setTokenCookie(response, result.getToken());

        // Không trả token trong body để tránh lưu vào localStorage
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .result(AuthenticationResponse.builder().authenticated(true).build())
                .build();
    }

    @PostMapping("/register")
    ApiResponse<Void> register(@RequestBody UserCreationRequest request) {
        authenticationService.register(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("OTP has been sent to your email. Please verify!")
                .build();
    }

    @PostMapping("/resend-otp")
    ApiResponse<Void> resendOtp(@RequestBody ResendOtpRequest request) {
        authenticationService.resendOTP(request.email());
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("OTP has been sent to your email")
                .build();
    }

    /**
     * Verify OTP – tạo user và set cookie JWT ngay sau khi xác minh.
     */
    @PostMapping("/verify")
    ApiResponse<AuthenticationResponse> verify(
            @RequestBody VerifyOtpRequest request,
            HttpServletResponse response) {

        var auth = authenticationService.verifyAndCreateUser(request.getEmail(), request.getOtpCode());
        setTokenCookie(response, auth.getToken());

        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .result(AuthenticationResponse.builder().authenticated(true).build())
                .build();
    }

    @PostMapping("/change-password")
    ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Password changed successfully!")
                .build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authenticationService.sendForgotPasswordOtp(request.getEmail());
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("OTP sent to your email.")
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Password reset successfully. You can login now.")
                .build();
    }

    /**
     * Login – xác thực username/password, set httpOnly cookie nếu thành công.
     * Rate limiting: tối đa 10 lần thử / phút mỗi IP.
     */
    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // ── Rate Limiting ──
        String clientIp = getClientIp(httpRequest);
        if (!rateLimitService.tryConsume(clientIp)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // ── Authenticate ──
        var result = authenticationService.authenticationResponse(request);
        setTokenCookie(httpResponse, result.getToken());

        // Không trả token trong body
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .result(AuthenticationResponse.builder().authenticated(true).build())
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws JOSEException, ParseException {

        try {
            String token = extractTokenFromCookie(httpRequest);
            if (token != null) {
                // Dùng LogoutRequest để tương thích với service layer hiện có
                authenticationService.logout(LogoutRequest.builder().token(token).build());
            }
        } catch (Exception e) {
            // Ignore any exception during token blacklisting to ensure the cookie is cleared
            // Exception will be logged by GlobalExceptionHandler if not caught here,
            // but we catch it here so we can proceed to clear the cookie.
        } finally {
            clearTokenCookie(httpResponse);
        }
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Logout successful!")
                .build();
    }

    /**
     * Refresh token – đọc token cũ từ cookie, tạo token mới, set cookie mới.
     */
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws JOSEException, ParseException {

        String token = extractTokenFromCookie(httpRequest);
        if (token == null || token.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var result = authenticationService.refreshToken(RefreshRequest.builder().token(token).build());
        setTokenCookie(httpResponse, result.getToken());

        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .result(AuthenticationResponse.builder().authenticated(true).build())
                .build();
    }

    // ── Utilities ──────────────────────────────────────────────────────────────

    /**
     * Lấy IP thực của client, xử lý cả reverse proxy (X-Forwarded-For).
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For có thể chứa chuỗi IP: "client, proxy1, proxy2"
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
