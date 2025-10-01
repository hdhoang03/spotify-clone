package com.spotify.spotify.service;

import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.CategoryMapper;
import com.spotify.spotify.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request){
        if(categoryRepository.findByName(request.getName()).isPresent()){
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
        Category category = categoryMapper.toCategory(request);
        category.setActive(true);
        category = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(category);
    }

    public List<CategoryResponse> getAllCategories(){
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(String id, CategoryRequest request){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryMapper.updateCategory(category, request);
        category = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(String id){
        if(!categoryRepository.existsById(id)){
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }

    public List<CategoryResponse> searchCategories(String keyword){
        return categoryRepository.findByNameContaining(keyword)
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
}
