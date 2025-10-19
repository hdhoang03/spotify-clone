package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = SongMapper.class)
public interface CategoryMapper {
    @Mapping(target = "coverUrl", ignore = true)
    Category toCategory(CategoryRequest request);
    @Mapping(target = "songs", ignore = true)
    CategoryResponse toCategoryResponse(Category category);
    @Mapping(target = "coverUrl", ignore = true)
    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
