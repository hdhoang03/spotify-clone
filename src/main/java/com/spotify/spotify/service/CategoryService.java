package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.constaint.CategoryType;
import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.request.CategoryUpdateRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.dto.response.CloudinaryResponse;
import com.spotify.spotify.dto.response.CustomPageImpl;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.CategoryMapper;
import com.spotify.spotify.repository.CategoryRepository;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.repository.CategoryRepository.CategoryWithSongCount;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

//Nhược điểm: Dữ liệu lưu sẽ là chuỗi nhị phân khó đọc bằng mắt khi dùng redis-cli.
public class CategoryService {
    CloudinaryService cloudinaryService;
    SongRepository songRepository;
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    //xóa sạch toàn bộ cache của Category mỗi khi có thay đổi (dùng allEntries = true).
    @CacheEvict(value = {"categories_page", "admin_categories_page", "category_detail", "categories_by_type"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request){
        if(categoryRepository.existsByNameIgnoreCase(request.getName())){
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        Category category = categoryMapper.toCategory(request);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getCoverUrl(), "spotify/categories");
            if (cloudRes != null) category.setCoverUrl(cloudRes.getUrl());
        }

        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    @CacheEvict(value = {"categories_page", "admin_categories_page", "category_detail", "categories_by_type", "songs_by_category"}, allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional //Rollback khi gặp lỗi
    public CategoryResponse addSongToCategory(String categoryId, List<String> songIds){
        Category category = categoryRepository.findByIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        List<Song> songs = songRepository.findAllById(songIds);
        if (songs.size() != songIds.size()){
            throw new AppException(ErrorCode.SONG_NOT_FOUND);
        }

        songs.forEach(song -> song.setCategory(category));
        songRepository.saveAll(songs);

        return categoryMapper.toCategoryResponse(category);
    }

    @CacheEvict(value = {"categories_page", "admin_categories_page", "category_detail", "categories_by_type"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void removeSongFromCategory(String categoryId, String songId){
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        if (song.getCategory() == null || !song.getCategory().getId().equals(categoryId)){
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        song.setCategory(null);
        songRepository.save(song);
    }

    @Cacheable(value = "categories_page", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CategoryResponse> getAllCategories(Pageable pageable){
        Page<CategoryWithSongCount> projections = categoryRepository.findAllWithSongCountByDeletedFalse(pageable);

        Page<CategoryResponse> mappedPage = projections.map(categoryMapper::toCategoryResponseFromProjection);
        return new CustomPageImpl<>(mappedPage.getContent(), pageable, mappedPage.getTotalElements());
    }

    @Cacheable(value = "category_detail", key = "#id")
    public CategoryResponse getCategoryById(String id){
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    @CacheEvict(value = {"categories_page", "admin_categories_page", "category_detail", "categories_by_type"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(String id, CategoryUpdateRequest request){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryMapper.updateCategory(category, request);

        //Kiểm tra có trùng tên không
        if (request.getName() != null && !category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())){
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        if(request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            if (category.getCoverUrl() != null){
                cloudinaryService.deleteFile(category.getCoverUrl(), "image");
            }
            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getCoverUrl(), "spotify/categories");
            if (cloudRes != null) category.setCoverUrl(cloudRes.getUrl());
        }

        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    @CacheEvict(value = {"categories_page", "admin_categories_page", "category_detail", "categories_by_type"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(String id){
        Category category = categoryRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getName().equalsIgnoreCase("Others")){
            throw new AppException(ErrorCode.CANNOT_DELETE_DEFAULT_CATEGORY);
        }

        Category defaultCategory = categoryRepository.findByName("Others")
                        .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        Set<Song> songs = category.getSongs();
        if (songs != null && !songs.isEmpty()){
            for (Song song : songs){
                song.setCategory(defaultCategory);
            }
        }
        category.setActive(false);
        category.setDeleted(true);//soft delete
        categoryRepository.save(category);
    }

    @CacheEvict(value = {"categories_page", "admin_categories_page", "category_detail", "categories_by_type"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreCategory(String id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setDeleted(false);
        category.setActive(true);
        categoryRepository.save(category);
    }

    @Cacheable(value = "admin_categories_page", key = "#keyword + '_' + #isDeleted + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CategoryResponse> searchCategories(String keyword, boolean isDeleted, Pageable pageable){
        var projections = categoryRepository.searchCategoriesWithCount(keyword, isDeleted, pageable);
        Page<CategoryResponse> mappedPage = projections.map(categoryMapper::toCategoryResponseFromProjection);
        return new CustomPageImpl<>(mappedPage.getContent(), pageable, mappedPage.getTotalElements());
    }

    @Cacheable(value = "categories_by_type", key = "#type.name()")
    public List<CategoryResponse> getCategoriesByType(CategoryType type){
        return categoryRepository.findByTypeAndDeletedFalseOrderByDisplayOrderAsc(type)
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"categories_page", "admin_categories_page", "category_detail", "categories_by_type"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateDisplayOrder(String id, Integer newOrder){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        category.setDisplayOrder(newOrder);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }
}
