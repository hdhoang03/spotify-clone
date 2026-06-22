package com.spotify.spotify.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SseService {
    Map<String, SseEmitter> emitters = new ConcurrentHashMap<>(); // Dùng ConcurrentHashMap để tránh lỗi khi có nhiều luồng cùng truy cập

    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); //30 phút
        emitters.put(username, emitter);

        //Dọn bộ nhớ khi ngắt kết nối hoặc hết hạn
        emitter.onCompletion(() -> emitters.remove(username));
        emitter.onTimeout(() -> emitters.remove(username));
        emitter.onError((e) -> emitters.remove(username));

        //gửi tin nhắn rỗng khi kết thúc kết nối để trình duyệt không bị timeout chờ
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected successfully"));
        } catch (IOException e) {
            emitters.remove(username);
        }
        return emitter;
    }

    public void sendNotification(String username, Object notificationData) {
        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                //tạo 1 event có tên là notification
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .data(notificationData));
            } catch (IOException e){
                emitters.remove(username); //nếu lỗi (tắt web) thì xóa luôn
            }
        }
    }

    public void broadcastNotification(Object notificationData) {
        emitters.forEach((username, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .data(notificationData));
            } catch (IOException e) {
                emitters.remove(username); //tắt web thì xóa khỏi map
            }
        });
    }
}
