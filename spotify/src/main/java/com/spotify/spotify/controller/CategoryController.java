package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.request.CategoryUpdateRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/categories")
public class CategoryController {
    CategoryService categoryService;

    @PostMapping("/create")
    ApiResponse<CategoryResponse> createCategory(@ModelAttribute CategoryRequest request){
        CategoryResponse response = categoryService.createCategory(request);
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Category has been created!")
                .result(response)
                .build();
    }

    @GetMapping()
    ApiResponse<Page<CategoryResponse>> getAllCategories(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size
    ){
        Page<CategoryResponse> responses = categoryService.getAllCategories(PageRequest.of(page - 1, size));
        return ApiResponse.<Page<CategoryResponse>>builder()
                .code(1000)
                .message("All categories fetched!")
                .result(responses)
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<CategoryResponse> getCategoryById(@PathVariable String id){
        CategoryResponse response = categoryService.getCategoryById(id);
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Category detail fetched!")
                .result(response)
                .build();
    }

    @PutMapping("/update/{id}")
    ApiResponse<CategoryResponse> updateCategory(@PathVariable String id, @ModelAttribute CategoryUpdateRequest request){
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Category has been updated!")
                .result(response)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    ApiResponse<Void> deleteCategory(@PathVariable String id){
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Category has been deleted!")
                .build();
    }

    @GetMapping("/search")
    ApiResponse<Page<CategoryResponse>> searchCategories(@RequestParam String keyword,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size
    ){
        Page<CategoryResponse> responses = categoryService.searchCategories(keyword, PageRequest.of(page - 1, size));
        return ApiResponse.<Page<CategoryResponse>>builder()
                .code(1000)
                .message("Categories search result!")
                .result(responses)
                .build();
    }

    @PostMapping("/{categoryId}/songs/{songId}")
    ApiResponse<CategoryResponse> addSongToCategory(@PathVariable String categoryId, @PathVariable String songId){
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Song has been added into category.")
                .result(categoryService.addSongToCategory(categoryId, songId))
                .build();
    }
    //Thêm endpoint xóa khỏi category
}