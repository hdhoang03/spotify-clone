package com.spotify.spotify.controller;

import com.spotify.spotify.constaint.CategoryType;
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

    @GetMapping("/list")
    ApiResponse<Page<CategoryResponse>> searchCategories(@RequestParam(defaultValue = "", required = false) String keyword,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "false") boolean isDeleted
    ){
        Page<CategoryResponse> responses = categoryService.searchCategories(keyword, isDeleted, PageRequest.of(page - 1, size));
        return ApiResponse.<Page<CategoryResponse>>builder()
                .code(1000)
                .message("Categories search result!")
                .result(responses)
                .build();
    }

    @PatchMapping("/restore/{id}")
    ApiResponse<Void> restoreCategory(@PathVariable String id){
        categoryService.restoreCategory(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Category has been restored")
                .build();
    }

    @PostMapping("/{categoryId}/songs")
    ApiResponse<CategoryResponse> addSongsToCategory(@PathVariable String categoryId,
                                                     @PathVariable List<String> songIds){
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Song has been added into category.")
                .result(categoryService.addSongToCategory(categoryId, songIds))
                .build();
    }

    @DeleteMapping("/{categoryId}/songs/{songId}")
    ApiResponse<Void> removeSongFromCategory(@PathVariable String categoryId, @PathVariable String songId){
        categoryService.removeSongFromCategory(categoryId, songId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Song has been removed from category")
                .build();
    }

    @GetMapping("/type/{type}")
    ApiResponse<List<CategoryResponse>> getCategoriesByType(@PathVariable CategoryType type){
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(1000)
                .result(categoryService.getCategoriesByType(type))
                .build();
    }

    @PutMapping("/{id}/display-order")
    ApiResponse<CategoryResponse> updateCategoryOrder(@PathVariable String id, @RequestParam Integer order){
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Display order updated!")
                .result(categoryService.updateDisplayOrder(id, order))
                .build();
    }
}