package com.spotify.spotify.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.PaymentRequest;
//import com.spotify.spotify.dto.request.WebhookRequest;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.service.OrderService;
import com.spotify.spotify.service.PayOSService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PayOSService payOSService;
    OrderService orderService;

    @PostMapping("/create-link")
    ApiResponse<?> createLink(@RequestBody PaymentRequest request){
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            orderService.createOrder(request, username);
            String jsonResponse = payOSService.createPaymentLink(request);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>(){});

            return ApiResponse.builder()
                    .code(1000)
                    .message("Order Created")
                    .result(result)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi tạo link thanh toán: ", e);
            return ApiResponse.builder()
                    .code(1000)
                    .message("Có lỗi xảy ra khi tạo đơn hàng: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/webhook")
    ApiResponse<String> webhook(@RequestBody JsonNode rawRequest){
        boolean isValid = payOSService.verifyWebhook(rawRequest);
        if(!isValid){
            log.error("Xác thực Webhook thất bại. Chữ ký không khớp!");
            return ApiResponse.<String>builder()
                    .code(400)
                    .message("Invalid signature")
                    .build();
        }

        JsonNode dataNode = rawRequest.path("data");
        String code = dataNode.path("code").asText();

        if ("00".equals(code)) {
            long orderCode = dataNode.path("orderCode").asLong();
            log.info("Order code is {}", orderCode);

            try {
                orderService.completePayment(orderCode);
            } catch (Exception e) {
                log.error("Exception occurred while sending payment", e);
            }
        }
        return ApiResponse.<String>builder()
                .code(200)
                .message("Success")
                .build();
    }
}
