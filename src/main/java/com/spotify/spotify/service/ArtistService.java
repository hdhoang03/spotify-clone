//package com.spotify.spotify.service;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import com.spotify.spotify.dto.request.ArtistRequest;
//import com.spotify.spotify.dto.response.AlbumResponse;
//import com.spotify.spotify.dto.response.ArtistResponse;
//import com.spotify.spotify.dto.response.CloudinaryResponse;
//import com.spotify.spotify.entity.Album;
//import com.spotify.spotify.entity.Artist;
//import com.spotify.spotify.entity.Song;
//import com.spotify.spotify.entity.User;
//import com.spotify.spotify.exception.AppException;
//import com.spotify.spotify.exception.ErrorCode;
//import com.spotify.spotify.mapper.AlbumMapper;
//import com.spotify.spotify.mapper.ArtistMapper;
//import com.spotify.spotify.repository.AlbumRepository;
//import com.spotify.spotify.repository.ArtistFollowRepository;
//import com.spotify.spotify.repository.ArtistRepository;
//import com.spotify.spotify.repository.UserRepository;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Slf4j
//public class ArtistService {
//    ArtistFollowRepository artistFollowRepository;
//    CloudinaryService cloudinaryService;
//    ArtistRepository artistRepository;
//    UserRepository userRepository;
//    ArtistMapper artistMapper;
//
//    @CacheEvict(value = {"artists_page", "admin_artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
//    @Transactional
//    @PreAuthorize("hasRole('ADMIN')")
//    public ArtistResponse createArtist(ArtistRequest request){
//        if (artistRepository.existsByNameIgnoreCase(request.getName())){
//            throw new AppException(ErrorCode.ARTIST_EXISTED);
//        }
//
//        Artist artist = artistMapper.toArtist(request);
//
//        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
//            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getAvatarUrl(), "spotify/artists");
//            if (cloudRes != null) artist.setAvatarUrl(cloudRes.getUrl());
//        }
//
//        artist = artistRepository.save(artist);
//        return artistMapper.toArtistResponse(artist);
//    }
//
//    @Cacheable(value = "artists_page", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
//    public Page<ArtistResponse> getAllArtists(Pageable pageable){
//        Page<ArtistRepository.ArtistWithSongCount> pageData = artistRepository.findAllWithSongCount(false, pageable);
//        if (pageData.isEmpty()){
//            return pageData.map(item -> artistMapper.toArtistResponse(item.getArtist()));
//        }
//
//        List<String> artistIdsInPage = pageData.getContent().stream()
//                .map(item -> item.getArtist().getId())
//                .toList();
//
//        Set<String> followedArtistIds = getFollowedArtistIdsForCurrentUser(artistIdsInPage);
//        return pageData.map(item -> {
//            ArtistResponse response = artistMapper.toArtistResponse(item.getArtist());
//            response.setSongCount(item.getSongCount().intValue());
//            response.setIsFollowed(followedArtistIds.contains(item.getArtist().getId()));
//            return response;
//        });
//    }
//
//    public Set<String> getFollowedArtistIdsForCurrentUser(List<String> artistIds){
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        if (username == null){
//            return Set.of();
//        }
//
//        return userRepository.findByUsername(username)
//                .map(user -> artistFollowRepository.findFollowedArtistIds(user.getId(), artistIds))
//                .orElse(Set.of());
//    }
//
//    public boolean checkIsFollowed(String artistId){
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        if (username == null){
//            return false;
//        }
//
//        return userRepository.findByUsername(username)
//                .map(user -> artistFollowRepository.existsByUserIdAndArtistId(user.getId(), artistId))
//                .orElse(false);
//    }
//
//    @CacheEvict(value = {"artists_page", "admin_artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
//    @Transactional
//    @PreAuthorize("hasRole('ADMIN')")
//    public ArtistResponse updateArtist(String id, ArtistRequest request){
//        Artist artist = artistRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
//
//        artistMapper.updateArtist(artist, request);
//
//        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()){
//            if (artist.getAvatarUrl() != null){ //Xóa ảnh cũ
//                cloudinaryService.deleteFile(artist.getAvatarUrl(), "image");
//            }
//
//            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getAvatarUrl(), "spotify/artists");
//            if (cloudRes != null) artist.setAvatarUrl(cloudRes.getUrl());
//        }
//
//        artist = artistRepository.save(artist);
//        return artistMapper.toArtistResponse(artist);
//    }
//
//    @CacheEvict(value = {"artists_page", "admin_artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
//    @Transactional
//    @PreAuthorize("hasRole('ADMIN')")
//    public void deleteArtist(String id){
//        Artist artist = artistRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
//
//        if (artist.isDeleted()) return;
//
//        artist.setDeleted(true);
//
//        artistRepository.save(artist);
//    }
//
//    @CacheEvict(value = {"artists_page", "admin_artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
//    @Transactional
//    @PreAuthorize("hasRole('ADMIN')")
//    public void restoreArtist(String id){
//        Artist artist = artistRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
//
//        if(!artist.isDeleted()) return;
//
//        artist.setDeleted(false);
//
//        artistRepository.save(artist);
//    }
//
//    @Cacheable(value = "artist_detail", key = "#id + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()", unless = "#result == null")
//    public ArtistResponse getArtistById(String id){
//        Artist artist = artistRepository.findByIdAndDeletedFalse(id)
//                .orElse(null);
//        if (artist == null) return null;
//
//        ArtistResponse response = artistMapper.toArtistResponse(artist);
//        boolean isFollowed = false;
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        if (username != null && !username.equals("anonymousUser")){
//            Optional<User> userOpt = userRepository.findByUsername(username);
//            if (userOpt.isPresent()){
//                isFollowed = artistFollowRepository.existsByUserIdAndArtistId(userOpt.get().getId(), id);
//            }
//        }
//        response.setIsFollowed(isFollowed);
//        long songCount = artistRepository.countSongsByArtistId(id);
//        response.setSongCount((int) songCount);
//        return response;
//    }
//
//    public Page<ArtistResponse> searchArtists(String keyword, boolean isDeleted, Pageable pageable){
//        Page<ArtistRepository.ArtistWithSongCount> projections = (keyword != null && !keyword.isBlank()
//                ? artistRepository.searchWithSongCount(keyword, isDeleted, pageable)
//                : artistRepository.findAllWithSongCount(isDeleted, pageable));
//
//        if (projections.isEmpty()){
//            return projections.map(p -> artistMapper.toArtistResponse(p.getArtist()));
//        }
//
//        List<String> artistIdsInPage = projections.getContent().stream()
//                .map(item -> item.getArtist().getId())
//                .toList();
//
//        Set<String> followedArtistIds = getFollowedArtistIdsForCurrentUser(artistIdsInPage);
//
//        return projections.map(projection -> {
//            ArtistResponse response = artistMapper.toArtistResponse(projection.getArtist());
//            response.setSongCount(projection.getSongCount() != null ? projection.getSongCount().intValue() : 0);
//            response.setIsFollowed(followedArtistIds.contains(projection.getArtist().getId()));
//            return response;
//        });
//    }
//}


package com.spotify.spotify.service;

import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.dto.response.CloudinaryResponse;
import com.spotify.spotify.dto.response.CustomPageImpl;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.ArtistMapper;
import com.spotify.spotify.repository.ArtistFollowRepository;
import com.spotify.spotify.repository.ArtistRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ArtistService {

    ArtistFollowRepository artistFollowRepository;
    CloudinaryService cloudinaryService;
    ArtistRepository artistRepository;
    UserRepository userRepository;
    ArtistMapper artistMapper;
    RedisTemplate<String, Object> redisTemplate;

    private static final String USER_ID_CACHE_KEY = "user_id_by_name::";   // TTL 30 phút
    private static final String FOLLOWED_IDS_KEY   = "followed_artist_ids::"; // TTL 5 phút
    // Số trang tối đa một user có thể đã truy cập — dùng để evict không cần KEYS scan
    private static final int MAX_CACHED_PAGES = 20;

    @CacheEvict(value = {"artists_page", "admin_artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ArtistResponse createArtist(ArtistRequest request) {
        if (artistRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.ARTIST_EXISTED);
        }
        Artist artist = artistMapper.toArtist(request);
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getAvatarUrl(), "spotify/artists");
            if (cloudRes != null) artist.setAvatarUrl(cloudRes.getUrl());
        }
        return artistMapper.toArtistResponse(artistRepository.save(artist));
    }

    @CacheEvict(value = {"artists_page", "admin_artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ArtistResponse updateArtist(String id, ArtistRequest request) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
        artistMapper.updateArtist(artist, request);
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            if (artist.getAvatarUrl() != null) {
                cloudinaryService.deleteFile(artist.getAvatarUrl(), "image");
            }
            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getAvatarUrl(), "spotify/artists");
            if (cloudRes != null) artist.setAvatarUrl(cloudRes.getUrl());
        }
        return artistMapper.toArtistResponse(artistRepository.save(artist));
    }

    @CacheEvict(value = {"artists_page", "admin_artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteArtist(String id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
        if (artist.isDeleted()) return;
        artist.setDeleted(true);
        artistRepository.save(artist);
    }

    @CacheEvict(value = {"artists_page", "admin_artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreArtist(String id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
        if (!artist.isDeleted()) return;
        artist.setDeleted(false);
        artistRepository.save(artist);
    }

    public Page<ArtistResponse> getAllArtists(Pageable pageable) {
        Page<ArtistResponse> cachedPage = getArtistPageCached(pageable);

        List<String> artistIds = cachedPage.getContent().stream()
                .map(ArtistResponse::getId)
                .collect(Collectors.toList());

        // Truyền page info để tạo key dự đoán — không dùng hash nữa
        Set<String> followedIds = getFollowedArtistIdsForCurrentUser(
                artistIds, pageable.getPageNumber(), pageable.getPageSize());

        List<ArtistResponse> enriched = cachedPage.getContent().stream()
                .map(r -> {
                    ArtistResponse copy = r.toBuilder().build();
                    copy.setIsFollowed(followedIds.contains(r.getId()));
                    return copy;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(enriched, pageable, cachedPage.getTotalElements());
    }

    @Cacheable(value = "artists_page", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ArtistResponse> getArtistPageCached(Pageable pageable) {
        Page<ArtistRepository.ArtistWithSongCount> pageData =
                artistRepository.findAllWithSongCount(false, pageable);
        Page<ArtistResponse> mappedPage = pageData.map(item -> {
            ArtistResponse response = artistMapper.toArtistResponse(item.getArtist());
            response.setSongCount(item.getSongCount().intValue());
            return response;
        });
        return new CustomPageImpl<>(mappedPage.getContent(), pageable, mappedPage.getTotalElements());
    }

    @Cacheable(
            value = "artist_detail",
            key   = "#id + '_' + T(org.springframework.security.core.context.SecurityContextHolder)" +
                    ".getContext().getAuthentication().getName()",
            unless = "#result == null"
    )
    public ArtistResponse getArtistById(String id) {
        Artist artist = artistRepository.findByIdAndDeletedFalse(id).orElse(null);
        if (artist == null) return null;

        ArtistResponse response = artistMapper.toArtistResponse(artist);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null && !username.equals("anonymousUser")) {
            userRepository.findByUsername(username).ifPresent(user -> {
                boolean isFollowed = artistFollowRepository
                        .existsByUserIdAndArtistId(user.getId(), id);
                response.setIsFollowed(isFollowed);
            });
        }

        response.setSongCount((int) artistRepository.countSongsByArtistId(id));
        return response;
    }

    @Cacheable(value = "admin_artists_page", key = "#keyword + '_' + #isDeleted + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ArtistResponse> searchArtists(String keyword, boolean isDeleted, Pageable pageable) {
        Page<ArtistRepository.ArtistWithSongCount> projections = (keyword != null && !keyword.isBlank())
                ? artistRepository.searchWithSongCount(keyword, isDeleted, pageable)
                : artistRepository.findAllWithSongCount(isDeleted, pageable);

        if (projections.isEmpty()) {
            Page<ArtistResponse> emptyPage = projections.map(p -> artistMapper.toArtistResponse(p.getArtist()));
            return new com.spotify.spotify.dto.response.CustomPageImpl<>(emptyPage.getContent(), pageable, emptyPage.getTotalElements());
        }

        List<String> artistIds = projections.getContent().stream()
                .map(item -> item.getArtist().getId())
                .collect(Collectors.toList());

        // searchArtists là admin-only, ít request hơn — không cache follow status
        // để tránh phức tạp hóa key (kết quả còn phụ thuộc vào keyword + isDeleted)
        Set<String> followedIds = getFollowedArtistIdsForCurrentUser(
                artistIds, pageable.getPageNumber(), pageable.getPageSize());

        Page<ArtistResponse> mappedPage = projections.map(projection -> {
            ArtistResponse response = artistMapper.toArtistResponse(projection.getArtist());
            response.setSongCount(projection.getSongCount() != null
                    ? projection.getSongCount().intValue() : 0);
            response.setIsFollowed(followedIds.contains(projection.getArtist().getId()));
            return response;
        });
        
        return new CustomPageImpl<>(mappedPage.getContent(), pageable, mappedPage.getTotalElements());
    }

    @SuppressWarnings("unchecked")
    public Set<String> getFollowedArtistIdsForCurrentUser(List<String> artistIds,
                                                          int pageNumber, int pageSize) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.equals("anonymousUser")) return new HashSet<>();

        // Tầng 1: lấy userId từ cache (tránh query bảng user mỗi request)
        String userIdKey = USER_ID_CACHE_KEY + username;
        String userId = null;
        try {
            userId = (String) redisTemplate.opsForValue().get(userIdKey);
        } catch (Exception ignored) {}

        if (userId == null) {
            Optional<com.spotify.spotify.entity.User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) return new HashSet<>();
            userId = userOpt.get().getId();
            try {
                redisTemplate.opsForValue().set(userIdKey, userId, 30, TimeUnit.MINUTES);
            } catch (Exception ignored) {}
        }

        String followedKey = FOLLOWED_IDS_KEY + username + "::" + pageNumber + "_" + pageSize;

        Set<String> followedIds = null;
        try {
            Object cached = redisTemplate.opsForValue().get(followedKey);
            if (cached instanceof Set) {
                followedIds = (Set<String>) cached;
            } else if (cached instanceof List) {
                followedIds = new HashSet<>((List<String>) cached);
            }
        } catch (Exception ignored) {}

        if (followedIds == null) {
            followedIds = artistFollowRepository.findFollowedArtistIds(userId, artistIds);
            try {
                // Lưu dưới dạng ArrayList để Jackson serialize an toàn
                redisTemplate.opsForValue().set(followedKey, new ArrayList<>(followedIds), 5, TimeUnit.MINUTES);
            } catch (Exception ignored) {}
        }

        return followedIds;
    }

    public void evictUserFollowCache(String username) {
        try {
            redisTemplate.delete(USER_ID_CACHE_KEY + username);
            int[] commonPageSizes = {10, 13, 20};
            List<String> keysToDelete = new ArrayList<>();
            for (int page = 0; page < MAX_CACHED_PAGES; page++) {
                for (int size : commonPageSizes) {
                    keysToDelete.add(FOLLOWED_IDS_KEY + username + "::" + page + "_" + size);
                }
            }
            redisTemplate.delete(keysToDelete);
        } catch (Exception e) {
            log.warn("[ArtistService] Can't delete cache for user {}: {}", username, e.getMessage());
        }
    }

    public boolean checkIsFollowed(String artistId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.equals("anonymousUser")) return false;
        return userRepository.findByUsername(username)
                .map(user -> artistFollowRepository.existsByUserIdAndArtistId(user.getId(), artistId))
                .orElse(false);
    }
}