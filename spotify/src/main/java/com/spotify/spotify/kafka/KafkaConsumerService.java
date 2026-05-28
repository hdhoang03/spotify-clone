package com.spotify.spotify.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.spotify.dto.event.NotificationEvent;
import com.spotify.spotify.dto.event.SseNotificationEvent;
import com.spotify.spotify.dto.response.NotificationResponse;
import com.spotify.spotify.entity.Notification;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.repository.ArtistFollowRepository;
import com.spotify.spotify.repository.NotificationRepository;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.repository.UserRepository;
import com.spotify.spotify.service.EmailService;
import com.spotify.spotify.service.SseService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConsumerService {
    SongRepository songRepository;
    EmailService emailService;
    ObjectMapper objectMapper;
    ArtistFollowRepository artistFollowRepository;
    NotificationRepository notificationRepository;
    SseService sseService;
    UserRepository userRepository;

    @Transactional
    @KafkaListener(topics = "play_count", groupId = "springtunes-group")
    public void ListenPlayCount(String message){
        String songId = message.replace("\"", "");
        log.info("EVENT: increment for song's Id: {}", songId);
        try {
            songRepository.incrementPlayCount(songId);
        } catch (Exception e){
            log.error("Error when update view: ", e);
        }
    }

    @Transactional
    @KafkaListener(topics = "notification_topic", groupId = "springtunes-group")
    public void listenNotification(String message){
        log.info("EVENT: Received Notification Request: {}", message);
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            if ("EMAIL".equals(event.getChannel())){
                emailService.sendHtmlEmail(
                        event.getRecipient(),
                        event.getSubject(),
                        event.getTemplate(),
                        event.getParam()
                );
                log.info("SUCCESS: Email sent to: {}", event.getRecipient());
            }
        } catch (JsonProcessingException e){
            log.error("Cannot parse notification event: ", e);
        } catch (Exception e){
            log.error("Error sending email: ", e);// Kafka sẽ tự động retry nếu có lỗi (mặc định 10 lần)
        }
    }

    @Transactional
    @KafkaListener(topics = "sse_topic", groupId = "springtunes-group")
    public void listenerSseNotification(String message){
        log.info("EVENT: Received SSE Notification Request: {}", message);
        try {
            SseNotificationEvent event = objectMapper.readValue(message, SseNotificationEvent.class);//Json -> object
            NotificationResponse payload = event.getNotificationPayload();

            switch (event.getTargetType()){
                case SPECIFIC_USER:
                    User user = userRepository.findByUsername(event.getTargetId()).orElse(null);
                    if (user != null){
                        saveNotificationToDB(user, payload); //lưu vào db
                        sseService.sendNotification(event.getTargetId(), payload); //bắn live qua sse
                    }
                    log.info("SUCCESS: Sent to: {}", event.getTargetId());
                    break;

                case ALL_USERS:
                    List<User> allUsers = userRepository.findAll();
                    for (User u : allUsers){
                        saveNotificationToDB(u, payload);
                    }
                    sseService.broadcastNotification(payload);
                    log.info("SUCCESS: Sent to: {}", event.getTargetId());
                    break;

                case ARTIST_FANS:
                    List<String> followers = artistFollowRepository.findFollowerUsernamesByArtistId(event.getTargetId());
                    for (String username : followers) {
                        userRepository.findByUsername(username).ifPresent(u -> {
                            saveNotificationToDB(u, payload);
                            sseService.sendNotification(username, payload);
                        });
                    }
                    log.info("SUCCESS: Sent to: {} fans of artist", followers.size());
                    break;
            }
        } catch (Exception e){
            log.error("Error sending SSE notification: ", e);
        }
    }

    private void saveNotificationToDB(User recipient, NotificationResponse payload){
        Notification notification = Notification.builder()
                .type(payload.getType())
                .title(payload.getTitle())
                .targetUrl(payload.getTargetUrl())
                .thumbnail(payload.getThumbnail())
                .isRead(false) //tim mới gửi = chưa đọc
                .createdAt(payload.getCreatedAt())
                .recipient(recipient)
                .message(payload.getMessage())
                .build();

        notification = notificationRepository.save(notification);
        payload.setId(notification.getId());
    }
}
