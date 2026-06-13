package com.spotify.spotify.dto.request;

import com.spotify.spotify.exception.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotBlank(message = "Username không được trống")
    String username;
    @Size(min = 5, message = "Mật khẩu ít nhất 5 ký tự")
    String password;

    @NotBlank(message = "Captcha token is required!")
    String captchaToken;
}