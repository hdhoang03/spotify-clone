package com.spotify.spotify.service;

import com.spotify.spotify.dto.response.RecaptchaResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CaptchaService {
    @Value("${google.recaptcha.secret}")
    @NonFinal
    String recaptchaSecret;

    RestTemplate restTemplate;

    public boolean verifyCaptcha(String captchaToken) {
        String verifyToken = "https://www.google.com/recaptcha/api/siteverify";

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecret);
        requestMap.add("response", captchaToken);

        try {
            ResponseEntity<RecaptchaResponse> response = restTemplate.postForEntity(
                    verifyToken,
                    requestMap,
                    RecaptchaResponse.class
            );

            RecaptchaResponse recaptchaResponse = response.getBody();
            if (recaptchaResponse != null && recaptchaResponse.isSuccess() && recaptchaResponse.getScore() >= 0.5) {
                return true;
            } else {
                if (recaptchaResponse != null) {
                    log.warn("Phát hiện Bot spam hoặc Captcha không hợp lệ! Điểm: {}, Lỗi từ Google: {}", recaptchaResponse.getScore(), recaptchaResponse.getErrorCodes());
                } else {
                    log.warn("Phát hiện Bot spam! Không nhận được response từ Google.");
                }
                return false;
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi Google reCAPCHA API: ", e);
            return false;
        }
    }
}
