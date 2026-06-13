package com.spotify.spotify.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.spotify.configuration.PayOSConfig;
import com.spotify.spotify.dto.request.PaymentRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSService {
    PayOSConfig payOSConfig;
    RestTemplate restTemplate;

//    public String createPaymentLink(PaymentRequest request) {
//        // 1. Tạo Signature (theo tài liệu PayOS: sắp xếp key và tạo HMAC-SHA256)
//        String signature = generateSignature(request);
//        request.setSignature(signature);
//
//        // 2. Chuẩn bị Header
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("x-client-id", payOSConfig.getClientId());
//        headers.set("x-api-key", payOSConfig.getApiKey());
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // 3. Gọi API của PayOS
//        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);
//        String url = "https://api-merchant.payos.vn/v2/payment-requests";
//
//        // Trả về JSON chứa link thanh toán (checkoutUrl)
//        return restTemplate.postForObject(url, entity, String.class);
//    }

    public String createPaymentLink(PaymentRequest request) {
        String signature = generateSignature(request);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", payOSConfig.getClientId());
        headers.set("x-api-key", payOSConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // TẠO PAYLOAD CHỈ CHỨA CÁC TRƯỜNG PAYOS CHẤP NHẬN (Lọc bỏ planType)
        Map<String, Object> payOSPayload = new HashMap<>();
        payOSPayload.put("orderCode", request.getOrderCode());
        payOSPayload.put("amount", request.getAmount());
        payOSPayload.put("description", request.getDescription());
        payOSPayload.put("cancelUrl", request.getCancelUrl());
        payOSPayload.put("returnUrl", request.getReturnUrl());
        payOSPayload.put("signature", signature);

        // Gọi API của PayOS (Dùng payOSPayload thay vì truyền thẳng request)
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payOSPayload, headers);
        String url = "https://api-merchant.payos.vn/v2/payment-requests";

        // Trả về JSON chứa link thanh toán (checkoutUrl)
        return restTemplate.postForObject(url, entity, String.class);
    }

    private String generateSignature(PaymentRequest request) {
        try {
            String data = "amount=" +  request.getAmount() +
                          "&cancelUrl=" +  request.getCancelUrl() +
                          "&description=" +  request.getDescription() +
                          "&orderCode=" +  request.getOrderCode() +
                          "&returnUrl=" +  request.getReturnUrl();

            String checksumKey = payOSConfig.getChecksumKey();
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    checksumKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmacSha256.init(secretKeySpec);

            byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Lỗi khi tạo signature cho PayOS", e);
            throw new RuntimeException("Không thể tạo signature cho thanh toán", e);
        }
    }

    public boolean verifyWebhook(JsonNode rawRequest){
        try {
            JsonNode dataNode = rawRequest.get("data");

            // Ép thẳng JsonNode sang Map<String, Object> để tránh lỗi type của Jackson
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> dataMap = mapper.convertValue(dataNode, new TypeReference<Map<String, Object>>() {});

            // Đưa vào TreeMap để tự động sắp xếp A-Z
            java.util.TreeMap<String, Object> sortedData = new java.util.TreeMap<>(dataMap);

            StringBuilder dataStr = new StringBuilder();
            for (Map.Entry<String, Object> entry : sortedData.entrySet()) {
                // Nếu giá trị null, ép thành chuỗi rỗng để không bị mất Key
                String value = (entry.getValue() == null) ? "" : String.valueOf(entry.getValue());
                dataStr.append(entry.getKey()).append("=").append(value).append("&");
            }

            if (dataStr.length() > 0) {
                dataStr.deleteCharAt(dataStr.length() - 1);
            }
            log.info("Chuỗi chuẩn bị băm signature: {}", dataStr.toString());
            String checksumKey = payOSConfig.getChecksumKey();
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    checksumKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmacSha256.init(secretKeySpec);
            byte[] hash = hmacSha256.doFinal(dataStr.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String calculatedSignature = hexString.toString();
            String payosSignature = rawRequest.get("signature").asText();
            log.info("Chữ ký tự tính toán (của tui): {}", calculatedSignature);
            log.info("Chữ ký PayOS gửi về (của PayOS): {}", payosSignature);
            return calculatedSignature.equals(payosSignature);
        } catch (Exception e) {
            log.error("Lỗi khi xác thực webhook signature cho PayOS", e);
            return false;
        }
    }
}
