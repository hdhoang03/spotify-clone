package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.Album;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.AlbumMapper;
import com.spotify.spotify.mapper.ArtistMapper;
import com.spotify.spotify.repository.AlbumRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ArtistService {
    ArtistFollowRepository artistFollowRepository;
    ArtistRepository artistRepository;
    UserRepository userRepository;
    ArtistMapper artistMapper;
    Cloudinary cloudinary;

    @CacheEvict(value = {"artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ArtistResponse createArtist(ArtistRequest request){
        if (artistRepository.existsByNameIgnoreCase(request.getName())){
            throw new AppException(ErrorCode.ARTIST_EXISTED);
        }

        Artist artist = artistMapper.toArtist(request);

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            String avatarPath = saveFileCloud(request.getAvatarUrl(), "spotify/artists");//Up ảnh lên cloud "spotify/artists"
            artist.setAvatarUrl(avatarPath);
        }
        artist = artistRepository.save(artist);
        return artistMapper.toArtistResponse(artist);
    }

    @Cacheable(value = "artists_page", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ArtistResponse> getAllArtists(Pageable pageable){
        Page<ArtistRepository.ArtistWithSongCount> pageData = artistRepository.findAllWithSongCount(false, pageable);
        if (pageData.isEmpty()){
            return pageData.map(item -> artistMapper.toArtistResponse(item.getArtist()));
        }

        List<String> artistIdsInPage = pageData.getContent().stream()
                .map(item -> item.getArtist().getId())
                .toList();

        Set<String> followedArtistIds = getFollowedArtistIdsForCurrentUser(artistIdsInPage);
        return pageData.map(item -> {
            ArtistResponse response = artistMapper.toArtistResponse(item.getArtist());
            response.setSongCount(item.getSongCount().intValue());
            response.setIsFollowed(followedArtistIds.contains(item.getArtist().getId()));
            return response;
        });
    }

    public Set<String> getFollowedArtistIdsForCurrentUser(List<String> artistIds){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null){
            return Set.of();
        }

        return userRepository.findByUsername(username)
                .map(user -> artistFollowRepository.findFollowedArtistIds(user.getId(), artistIds))
                .orElse(Set.of());
    }

    public boolean checkIsFollowed(String artistId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null){
            return false;
        }

        return userRepository.findByUsername(username)
                .map(user -> artistFollowRepository.existsByUserIdAndArtistId(user.getId(), artistId))
                .orElse(false);
    }

    @CacheEvict(value = {"artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ArtistResponse updateArtist(String id, ArtistRequest request){
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        artistMapper.updateArtist(artist, request);

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()){
            if (artist.getAvatarUrl() != null){ //Xóa ảnh cũ
                deleteFileCloud(artist.getAvatarUrl(), "image");
            }
            String avatarPath = saveFileCloud(request.getAvatarUrl(), "spotify/artists");
            artist.setAvatarUrl(avatarPath);
        }
        artist = artistRepository.save(artist);
        return artistMapper.toArtistResponse(artist);
    }

    @CacheEvict(value = {"artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteArtist(String id){
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        if (artist.isDeleted()) return;

        artist.setDeleted(true);

//        if (artist.getSongs() != null || !artist.getSongs().isEmpty()){
//            artist.getSongs().forEach(song -> song.setDeleted(true));
//        }

        artistRepository.save(artist);
    }

    @CacheEvict(value = {"artists_page", "artist_detail", "albums_by_artist"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreArtist(String id){
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        if(!artist.isDeleted()) return;

        artist.setDeleted(false);

//        if (artist.getSongs() != null && !artist.getSongs().isEmpty()){
//            artist.getSongs().forEach(song -> song.setDeleted(false));
//        }

        artistRepository.save(artist);
    }

    @Cacheable(value = "artist_detail", key = "#id")
    public ArtistResponse getArtistById(String id){
        Artist artist = artistRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        ArtistResponse response = artistMapper.toArtistResponse(artist);
        boolean isFollowed = false;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null && !username.equals("anonymousUser")){
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()){
                isFollowed = artistFollowRepository.existsByUserIdAndArtistId(userOpt.get().getId(), id);
            }
        }
        response.setIsFollowed(isFollowed);
        long songCount = artistRepository.countSongsByArtistId(id);
        response.setSongCount((int) songCount);
        return response;
    }

    public Page<ArtistResponse> searchArtists(String keyword, boolean isDeleted, Pageable pageable){
        Page<ArtistRepository.ArtistWithSongCount> projections = (keyword != null && !keyword.isBlank()
                ? artistRepository.searchWithSongCount(keyword, isDeleted, pageable)
                : artistRepository.findAllWithSongCount(isDeleted, pageable));

        if (projections.isEmpty()){
            return projections.map(p -> artistMapper.toArtistResponse(p.getArtist()));
        }

        List<String> artistIdsInPage = projections.getContent().stream()
                .map(item -> item.getArtist().getId())
                .toList();

        Set<String> followedArtistIds = getFollowedArtistIdsForCurrentUser(artistIdsInPage);

        return projections.map(projection -> {
            ArtistResponse response = artistMapper.toArtistResponse(projection.getArtist());
            response.setSongCount(projection.getSongCount() != null ? projection.getSongCount().intValue() : 0);
            response.setIsFollowed(followedArtistIds.contains(projection.getArtist().getId()));
            return response;
        });
    }

    private String saveFileCloud(MultipartFile file, String folder){
        if(file == null || file.isEmpty()) return null;
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (Exception e){
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String getPublicIdFromUrl(String url){
        if (url == null || url.isEmpty()) return null;
        try {
            Pattern pattern = Pattern.compile("upload/(?:v\\d+/)?([^.]+)\\.[a-z0-9]+$");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()){
                return matcher.group(1);
            }
            return null;
        } catch (Exception e){
            log.error("Error parsing Public ID from URL: {}", url);
            return null;
        }

    }

    private void deleteFileCloud(String url, String resourceType){
        String publicId = getPublicIdFromUrl(url);
        if (publicId != null){
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
                log.info("Deleted file on Cloudinary: {} (Type: {})", publicId, resourceType);
            } catch (Exception e){
                log.error("Failed to delete file on Cloudinary: {}", publicId);
            }
        }
    }
}
