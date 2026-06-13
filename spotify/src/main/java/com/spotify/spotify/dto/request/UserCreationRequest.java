package com.spotify.spotify.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 4, message = "Username phải ít nhất 4 ký tự")
    String username;
    @Size(min = 6, message = "Mật khẩu ít nhất 6 ký tự")
    String password;
    LocalDate dob;
    String name;
    @Email(message = "Email không đúng định dạng")
    String email;

    @NotBlank(message = "Captcha token is required!")
    String captchaToken;
}