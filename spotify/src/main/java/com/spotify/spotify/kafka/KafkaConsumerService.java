package com.spotify.spotify.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.spotify.dto.event.NotificationEvent;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.service.EmailService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConsumerService {
    SongRepository songRepository;
    EmailService emailService;
    ObjectMapper objectMapper;

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
}
