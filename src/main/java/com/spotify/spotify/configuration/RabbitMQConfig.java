package com.spotify.spotify.configuration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "springtunes_exchange";
    public static final String PLAY_COUNT_QUEUE = "play_count_queue";
    public static final String NOTIFICATION_QUEUE = "notification_queue";
    public static final String SSE_QUEUE = "sse_queue";

    //Exchange
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    //Queue
    @Bean
    public Queue playCountQueue() {
        return new Queue(PLAY_COUNT_QUEUE, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Queue sseQueue() {
        return new Queue(SSE_QUEUE, true);
    }

    @Bean
    public Binding playCountBinding(Queue playCountQueue, DirectExchange exchange) {
        return BindingBuilder.bind(playCountQueue).to(exchange).with(PLAY_COUNT_QUEUE);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(notificationQueue).to(exchange).with(NOTIFICATION_QUEUE);
    }

    @Bean
    public Binding sseBinding(Queue sseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(sseQueue).to(exchange).with(SSE_QUEUE);
    }
}