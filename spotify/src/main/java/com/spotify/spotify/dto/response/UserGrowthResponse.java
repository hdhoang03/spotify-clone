package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserGrowthResponse {
    String date;
    Long users; //Sô lượng người dùng mới
}
