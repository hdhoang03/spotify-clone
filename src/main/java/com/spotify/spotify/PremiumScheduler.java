package com.spotify.spotify;

import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PremiumScheduler {
    UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void scanAndRevokeExpiredPremium() {
        log.info("Bắt đầu quét và hủy trạng thái Premium của các tài khoản hết hạn...");

        int updatedCount = userRepository.deactiveExpiredPremiumUsers(LocalDateTime.now());
        if (updatedCount > 0) {
            log.info("Đã hủy trạng thái Premium thành công cho {} người dùng.", updatedCount);
        } else {
            log.info("Không có tài khoản nào hết hạn Premium hôm nay.");
        }
    }
}
