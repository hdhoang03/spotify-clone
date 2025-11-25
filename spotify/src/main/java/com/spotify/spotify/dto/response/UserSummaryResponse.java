package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSummaryResponse {
    String id;
    String username;
    String avatarUrl;
}
