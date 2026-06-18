package com.spotify.spotify.dto.request;

import com.spotify.spotify.dto.response.RoleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileUpdateRequest {
    String name;
    MultipartFile avatar;
    Boolean isRemoved;
}
