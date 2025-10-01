package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = SongMapper.class)
public interface CategoryMapper {
    Category toCategory(CategoryRequest request);
    CategoryResponse toCategoryResponse(Category category);
    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
