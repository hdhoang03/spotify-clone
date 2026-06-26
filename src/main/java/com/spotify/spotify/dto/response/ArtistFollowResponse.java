package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArtistFollowResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    String id;
    String name;
    String avatarUrl;
}
