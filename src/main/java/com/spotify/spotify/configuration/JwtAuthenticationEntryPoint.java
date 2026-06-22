package com.spotify.spotify.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);//Trả dữ liệu về cái gì

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        //Chuyển ApiResponse thành JSON phản hồi
        ObjectMapper objectMapper = new ObjectMapper();//Chuyển đối tượng thành JSON
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));//convert apiResponse về chuỗi JSON này gửi phản hồi HTTP
        response.flushBuffer();//Đảm bảo dữ liệu gửi ngay
    }
}
