package com.spotify.spotify.service;

import com.spotify.spotify.constaint.NotificationTargetType;
import com.spotify.spotify.dto.event.SseNotificationEvent;
import com.spotify.spotify.dto.request.AdminNotificationRequest;
import com.spotify.spotify.dto.response.NotificationResponse;
import com.spotify.spotify.entity.Notification;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.kafka.KafkaProducerService;
import com.spotify.spotify.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NotificationService {
    KafkaProducerService kafkaProducerService;
    NotificationRepository notificationRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void broadcastSystemNotification(AdminNotificationRequest request){
        NotificationResponse payload = NotificationResponse.builder()
                .type("SYSTEM_MAINTENANCE")
                .title(request.getTitle())
                .message(request.getMessage())
                .targetUrl(request.getTargetUrl() != null ? request.getTargetUrl() : "/")
                .thumbnail("https://static.vecteezy.com/system/resources/previews/000/378/113/original/notification-vector-icon.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        SseNotificationEvent event = SseNotificationEvent.builder()
                .targetType(NotificationTargetType.ALL_USERS)
                .targetId(null) //tất cả nên không cần id cụ thể
                .notificationPayload(payload)
                .build();

        kafkaProducerService.sendMessage("sse_topic", event);
    }

    public Page<NotificationResponse> getMyNotifications(Pageable pageable){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return notificationRepository.findByRecipient_UsernameOrderByCreatedAtDesc(username, pageable)
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .targetUrl(n.getTargetUrl())
                        .thumbnail(n.getThumbnail())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    public long getUnreadCount(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificationRepository.countByRecipient_UsernameAndIsReadFalse(username);
    }

    @Transactional
    public boolean toggleReadStatus(String id){
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        boolean newStatus = !notification.isRead();
        notification.setRead(newStatus);
        notificationRepository.save(notification);

        return newStatus;
    }

    @Transactional
    public void markAllAsRead(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        notificationRepository.markAllAsRead(username);
    }

    @Transactional
    public void deleteNotification(String id){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Notification notification = notificationRepository.findByIdAndRecipient_Username(id, username)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notificationRepository.delete(notification);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoCleanSpamData(){
        LocalDateTime cutoff = LocalDateTime.now().minusDays(15);
        notificationRepository.cleanOldNotifications(cutoff);
    }
}
