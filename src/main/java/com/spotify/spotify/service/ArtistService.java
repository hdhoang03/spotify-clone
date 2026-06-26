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
import com.spotify.spotify.entity.User;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    // ─────────────────────────────────────────────
    //  ADMIN: tạo / sửa / xóa / khôi phục
    // ─────────────────────────────────────────────

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

    // ─────────────────────────────────────────────
    //  PUBLIC: lấy danh sách / chi tiết
    // ─────────────────────────────────────────────

    /**
     * getAllArtists — cache an toàn.
     *
     * VẤN ĐỀ CŨ:
     *   Cache cả `isFollowed` vào Redis → dữ liệu của user A bị trả cho user B.
     *   Hơn nữa, `getFollowedArtistIdsForCurrentUser` gọi DB trong mỗi request
     *   nên cache hầu như vô dụng (Spring cache key không tính username).
     *
     * CÁCH FIX:
     *   Bước 1 — Cache PHẦN TĨNH (danh sách artist + songCount) theo page/size.
     *            Không cache isFollowed vì nó khác nhau theo từng user.
     *   Bước 2 — Sau khi lấy từ cache, truy vấn isFollowed cho user hiện tại
     *            bằng 1 query duy nhất (IN clause) thay vì N query.
     *
     * Kết quả: cache hoạt động đúng, isFollowed luôn chính xác theo user.
     */
    public Page<ArtistResponse> getAllArtists(Pageable pageable) {
        // Bước 1: lấy dữ liệu tĩnh từ cache (hoặc DB nếu cache miss)
        Page<ArtistResponse> cachedPage = getArtistPageCached(pageable);

        // Bước 2: lấy danh sách artistId trong trang hiện tại
        List<String> artistIds = cachedPage.getContent().stream()
                .map(ArtistResponse::getId)
                .collect(Collectors.toList());

        // Bước 3: 1 query duy nhất để biết user đang follow những ai
        Set<String> followedIds = getFollowedArtistIdsForCurrentUser(artistIds);

        // Bước 4: gán isFollowed vào response (không mutate cache vì đây là list mới)
        List<ArtistResponse> enriched = cachedPage.getContent().stream()
                .map(r -> {
                    // Clone để không sửa object đang nằm trong cache
                    ArtistResponse copy = r.toBuilder().build();
                    copy.setIsFollowed(followedIds.contains(r.getId()));
                    return copy;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(enriched, pageable, cachedPage.getTotalElements());
    }

    /**
     * Cache PHẦN TĨNH — chỉ artist info + songCount, KHÔNG có isFollowed.
     * Key theo page + size (không tính sort vì thường cố định).
     */
    @Cacheable(value = "artists_page", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ArtistResponse> getArtistPageCached(Pageable pageable) {
        Page<ArtistRepository.ArtistWithSongCount> pageData =
                artistRepository.findAllWithSongCount(false, pageable);
        Page<ArtistResponse> mappedPage = pageData.map(item -> {
            ArtistResponse response = artistMapper.toArtistResponse(item.getArtist());
            response.setSongCount(item.getSongCount().intValue());
            // isFollowed để mặc định false — sẽ được gán đúng ở getAllArtists()
            return response;
        });
        return new CustomPageImpl<>(mappedPage.getContent(), pageable, mappedPage.getTotalElements());
    }

    /**
     * getArtistById — cache theo id + username.
     *
     * `unless = "#result == null"`: nếu artist không tồn tại (soft-deleted),
     * không cache null → tránh lỗi IllegalArgumentException từ disableCachingNullValues.
     */
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

    // ─────────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────────

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

        Set<String> followedIds = getFollowedArtistIdsForCurrentUser(artistIds);

        Page<ArtistResponse> mappedPage = projections.map(projection -> {
            ArtistResponse response = artistMapper.toArtistResponse(projection.getArtist());
            response.setSongCount(projection.getSongCount() != null
                    ? projection.getSongCount().intValue() : 0);
            response.setIsFollowed(followedIds.contains(projection.getArtist().getId()));
            return response;
        });
        
        return new CustomPageImpl<>(mappedPage.getContent(), pageable, mappedPage.getTotalElements());
    }

    // ─────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────

    /**
     * 1 query IN để lấy tất cả artistId mà user đang follow trong trang hiện tại.
     * Tránh N query kiểu existsByUserIdAndArtistId cho từng artist.
     */
    public Set<String> getFollowedArtistIdsForCurrentUser(List<String> artistIds) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.equals("anonymousUser")) return Set.of();
        return userRepository.findByUsername(username)
                .map(user -> artistFollowRepository.findFollowedArtistIds(user.getId(), artistIds))
                .orElse(Set.of());
    }

    public boolean checkIsFollowed(String artistId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.equals("anonymousUser")) return false;
        return userRepository.findByUsername(username)
                .map(user -> artistFollowRepository.existsByUserIdAndArtistId(user.getId(), artistId))
                .orElse(false);
    }
}