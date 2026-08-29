package com.spotify.spotify.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Service – bảo vệ endpoint login khỏi brute-force.
 *
 * <p>Chiến lược: Token Bucket (Bucket4j in-memory)
 * <ul>
 *   <li>Mỗi IP được phép thử login tối đa {@value #MAX_REQUESTS} lần / {@value #WINDOW_MINUTES} phút.</li>
 *   <li>Bucket được tạo lazily, tự cleanup sau {@value #CLEANUP_INTERVAL_MS}ms không hoạt động.</li>
 * </ul>
 *
 * <p>Giới hạn: In-memory → reset khi restart. Dùng Redis-backed Bucket4j nếu scale multi-instance.
 */
@Service
@Slf4j
public class RateLimitService {

    /** Số request tối đa trong khoảng thời gian WINDOW_MINUTES */
    private static final long MAX_REQUESTS = 10;

    /** Cửa sổ thời gian tính lại hạn mức (phút) */
    private static final long WINDOW_MINUTES = 1;

    /** Interval xóa bucket cũ (ms) — mỗi 10 phút */
    private static final long CLEANUP_INTERVAL_MS = 10 * 60 * 1000L;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastAccessTime = new ConcurrentHashMap<>();

    /**
     * Kiểm tra xem IP có được phép thực hiện request không.
     *
     * @param ip địa chỉ IP của client
     * @return {@code true} nếu còn quota, {@code false} nếu bị rate-limited
     */
    public boolean tryConsume(String ip) {
        Bucket bucket = buckets.computeIfAbsent(ip, this::createBucket);
        lastAccessTime.put(ip, System.currentTimeMillis());
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("[RateLimit] IP {} bị chặn – vượt quá {} lần/{} phút", ip, MAX_REQUESTS, WINDOW_MINUTES);
        }
        return allowed;
    }

    private Bucket createBucket(String ip) {
        log.debug("[RateLimit] Tạo bucket mới cho IP: {}", ip);
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_REQUESTS)
                .refillIntervally(MAX_REQUESTS, Duration.ofMinutes(WINDOW_MINUTES))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Scheduled cleanup – xóa bucket của các IP không hoạt động trong 10 phút.
     * Ngăn memory leak khi có nhiều IP thử tấn công rồi biến mất.
     */
    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MS)
    public void cleanupStaleBuckets() {
        long now = System.currentTimeMillis();
        long staleThreshold = CLEANUP_INTERVAL_MS;
        int before = buckets.size();

        lastAccessTime.entrySet().removeIf(entry -> {
            boolean stale = (now - entry.getValue()) > staleThreshold;
            if (stale) buckets.remove(entry.getKey());
            return stale;
        });

        int removed = before - buckets.size();
        if (removed > 0) {
            log.debug("[RateLimit] Cleanup: đã xóa {} bucket cũ, còn lại {}", removed, buckets.size());
        }
    }
}
