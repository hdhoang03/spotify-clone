package com.spotify.spotify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.spotify.configuration.RabbitMQConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cloudinary.json.JSONException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RabbitMQProducerService {
    RabbitTemplate rabbitTemplate;
    ObjectMapper objectMapper;

    public void sendMessage(String routingKey, Object object) {
        try {
            String message = objectMapper.writeValueAsString(object);
            log.info("LOG: Đang gửi vào Exchange: {} | RoutingKey (Queue): {} | Message: {}",
                    RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
        } catch (JsonProcessingException e) {
            log.error("Can not parse object to JSON", e);
        }
    }
}
