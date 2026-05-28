package com.spotify.spotify.dto.event;

import com.spotify.spotify.constaint.NotificationTargetType;
import com.spotify.spotify.dto.response.NotificationResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SseNotificationEvent {
    NotificationTargetType targetType; //loại đối tượng nhận
    String targetId; // loại người được nhận
    NotificationResponse notificationPayload;
}
