package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSummaryResponse implements Serializable {
    static final long serialVersionUID = 1L;
    String id;
    String username;
    String avatarUrl;
}
