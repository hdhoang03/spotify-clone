package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.ArtistMapper;
import com.spotify.spotify.repository.ArtistRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ArtistService {
    ArtistRepository artistRepository;
    ArtistMapper artistMapper;
    Cloudinary cloudinary;

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

//    public Page<ArtistResponse> getAllArtists(Pageable pageable){
//        return artistRepository.findAllByDeleted(pageable)
//                .map(artistMapper::toArtistResponse);
//    }

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

    public ArtistResponse getArtistById(String id){
        Artist artist = artistRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
        return artistMapper.toArtistResponse(artist);
    }

    public Page<ArtistResponse> searchArtists(String keyword, boolean isDeleted, Pageable pageable){
        Page<ArtistRepository.ArtistWithSongCount> projections = (keyword != null && !keyword.isBlank()
                ? artistRepository.searchWithSongCount(keyword, isDeleted, pageable)
                : artistRepository.findAllWithSongCount(isDeleted, pageable));

        return projections.map(projection -> {
            ArtistResponse response = artistMapper.toArtistResponse(projection.getArtist());
            response.setSongCount(projection.getSongCount() != null ? projection    .getSongCount().intValue() : 0);
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
