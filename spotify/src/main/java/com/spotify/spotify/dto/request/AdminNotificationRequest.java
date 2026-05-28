package com.spotify.spotify.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminNotificationRequest {
    String title;
    String message;
    String targetUrl;
}
