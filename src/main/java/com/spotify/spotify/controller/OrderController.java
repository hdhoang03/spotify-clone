package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.service.OrderService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @GetMapping("/{orderCode}")
    ApiResponse<Void> getOrder(@PathVariable("orderCode") Long orderCode) {
        orderService.completePayment(orderCode);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Ok")
                .build();
    }
}
