package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.constaint.CategoryType;
import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.request.CategoryUpdateRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.CategoryMapper;
import com.spotify.spotify.repository.CategoryRepository;
import com.spotify.spotify.repository.SongRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
public class CategoryService {
    Cloudinary cloudinary;
    SongRepository songRepository;
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request){
        if(categoryRepository.existsByNameIgnoreCase(request.getName())){
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        Category category = categoryMapper.toCategory(request);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            String coverPath = saveFileCloud(request.getCoverUrl(), "spotify/categories");//"spotify/categories"
            category.setCoverUrl(coverPath);
        }
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

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

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void removeSongFromCategory(String categoryId, String songId){
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        if (song.getCategory() != null || !song.getCategory().getId().equals(categoryId)){
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        song.setCategory(null);
        songRepository.save(song);
    }

    public Page<CategoryResponse> getAllCategories(Pageable pageable){
        return categoryRepository.findAllByDeletedFalse(pageable)
                .map(categoryMapper::toCategoryResponse);
    }

    public CategoryResponse getCategoryById(String id){
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

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
                deleteFileCloud(category.getCoverUrl(), "image");
            }
            String coverPath = saveFileCloud(request.getCoverUrl(), "spotify/categories");
            category.setCoverUrl(coverPath);
        }
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

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

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreCategory(String id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setDeleted(false);
        category.setActive(true);
        categoryRepository.save(category);
    }

    public Page<CategoryResponse> searchCategories(String keyword, boolean isDeleted, Pageable pageable){
        var projections = categoryRepository.searchCategoriesWithCount(keyword, isDeleted, pageable);
        return projections.map(categoryMapper::toCategoryResponseFromProjection);
    }

    public List<CategoryResponse> getCategoriesByType(CategoryType type){
        return categoryRepository.findByTypeAndDeletedFalseOrderByDisplayOrderAsc(type)
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateDisplayOrder(String id, Integer newOrder){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        category.setDisplayOrder(newOrder);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    private String saveFileCloud(MultipartFile file, String folder){
        if(file == null || file.isEmpty()) return null;
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder,
                            "resource_type", "auto"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (Exception e){
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String getPublicFromUrl(String url){
        if (url == null || url.isEmpty()) return null;
        try {
            Pattern pattern = Pattern.compile("upload/(?:v\\d+/)?([^.]+)\\.[a-z0-9]+$");
            Matcher matcher = pattern.matcher(url);
            return matcher.find() ? matcher.group(1) : null;
        } catch (Exception e){
            log.error("Error parsing Public ID: {}", url);
            return null;
        }
    }

    private void deleteFileCloud(String url, String resourceType){
        String publicId = getPublicFromUrl(url);
        if (publicId != null){
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
                log.info("Deleted Cloudinary file: {}", publicId);
            } catch (Exception e){
                log.error("Failed to deleted Cloudinary file: {}", publicId);
            }
        }
    }
}
