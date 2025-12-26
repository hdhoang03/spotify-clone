package com.spotify.spotify.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KafkaProducerService {
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper;

    public void sendMessage(String topic, Object object){
        try {
            String message = objectMapper.writeValueAsString(object);
            log.info("LOG: Đang gửi vào Topic: {} | Message: {}", topic, message);
            kafkaTemplate.send(topic, message);
        } catch (JsonProcessingException e){
            log.error("Can not parse Object to Json: ", e);
        }
    }
}
