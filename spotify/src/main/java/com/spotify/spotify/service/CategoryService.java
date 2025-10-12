package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.CategoryMapper;
import com.spotify.spotify.repository.CategoryRepository;
import com.spotify.spotify.repository.SongRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    Cloudinary cloudinary;
    SongRepository songRepository;
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request){
        Category category = categoryMapper.toCategory(request);
        if(categoryRepository.findByName(request.getName()).isPresent()){
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            String coverPath = saveFileCloud(request.getCoverUrl());
            category.setCoverUrl(coverPath);
        }
        category.setActive(true);
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse addSongToCategory(String categoryId, String songId){
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

//        category.getSongs().add(song);
//        categoryRepository.save(category);

        song.setCategory(category);
        category.getSongs().add(song);
        songRepository.save(song);

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
        if(request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            String coverPath = saveFileCloud(request.getCoverUrl());
            category.setCoverUrl(coverPath);
        }
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

    private String saveFileCloud(MultipartFile file){
        if(file == null || file.isEmpty()) return null;
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (Exception e){
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
