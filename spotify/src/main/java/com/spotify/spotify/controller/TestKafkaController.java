package com.spotify.spotify.controller;

import com.spotify.spotify.kafka.KafkaProducerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-kafka")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestKafkaController {
    KafkaProducerService kafkaProductService;

//    @PostMapping
//    public String testSend(@RequestParam String message){
//        kafkaProductService.sendMessage(message);
//        return "Send message: " + message;
//    }
}
