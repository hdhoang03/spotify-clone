package com.spotify.spotify.dto.request;

import com.spotify.spotify.constaint.CategoryType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryUpdateRequest {
    String name;
    MultipartFile coverUrl;
    String description;
    Boolean active;
    CategoryType type;
    String backgroundColor;
    Integer displayOrder;
}