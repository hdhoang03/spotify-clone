package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.request.CategoryUpdateRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.repository.CategoryRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = SongMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "songs", ignore = true)
    Category toCategory(CategoryRequest request);

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "id", source = "projection.category.id")
    @Mapping(target = "name", source = "projection.category.name")
    @Mapping(target = "description", source = "projection.category.description")
    @Mapping(target = "backgroundColor", source = "projection.category.backgroundColor")
    @Mapping(target = "coverUrl", source = "projection.category.coverUrl")
    @Mapping(target = "type", source = "projection.category.type")
    @Mapping(target = "songCount", source = "projection.songCount")
    @Mapping(target = "active", source = "projection.category.active")
    CategoryResponse toCategoryResponseFromProjection(CategoryRepository.CategoryWithSongCount projection);

    @Mapping(target = "coverUrl", ignore = true)
    void updateCategory(@MappingTarget Category category, CategoryUpdateRequest request);
}
