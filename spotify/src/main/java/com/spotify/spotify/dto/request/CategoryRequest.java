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
public class CategoryRequest {
    String name;
    MultipartFile coverUrl;
    CategoryType type;
    String description;
    String backgroundColor;
    Integer displayOrder;
    Boolean active;
}