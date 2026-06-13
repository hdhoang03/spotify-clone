package com.spotify.spotify.service;

import com.spotify.spotify.dto.request.PaymentRequest;
import com.spotify.spotify.entity.Order;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.repository.OrderRepository;

import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    EmailService emailService;
    UserRepository userRepository;

    @Transactional
    public void createOrder(PaymentRequest request, String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Order order = Order.builder()
                .orderCode(request.getOrderCode())
                .amount(request.getAmount())
                .description(request.getDescription())
                .planType(request.getPlanType())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        orderRepository.save(order);
        log.info("Order Created");
    }

    @Transactional
    public void completePayment(long orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_CODE_NOT_FOUND));

        if ("PAID".equals(order.getStatus())) {
            log.info("Đơn hàng {} đã được cập nhật trạng thái PAID trước đó. Bỏ qua xử lý trùng.", orderCode);
            return;
        }

        // 3. Cập nhật trạng thái
        order.setStatus("PAID");
        User user = order.getUser();
        long monthsToAdd = switch (order.getPlanType()){
            case QUARTERLY -> 3;
            case YEARLY -> 12;
            default -> 1;
        };

        user.setPremiumExpiryDate(LocalDateTime.now().plusMonths(monthsToAdd));
        user.setIsPremium(true);

        userRepository.save(user);
        orderRepository.save(order);
        log.info("Cập nhật trạng thái đơn hàng {} thành PAID thành công trong Database.", orderCode);

        // 4. Mở rộng: Gửi Email xác nhận thanh toán (Chạy Async không block luồng)
        if (order.getUserEmail() != null && !order.getUserEmail().isEmpty()) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderCode", order.getOrderCode());
            variables.put("amount", order.getAmount());
            variables.put("description", order.getDescription());

            // Gọi hàm sendHtmlEmail từ EmailService.java của bạn
            emailService.sendHtmlEmail(
                    order.getUserEmail(),
                    "Xác nhận thanh toán thành công - SpringTunes",
                    "payment-success", // Tên file template HTML
                    variables
            );
            log.info("Đã tạo luồng gửi email xác nhận cho đơn hàng: {}", orderCode);
        }
    }
}
