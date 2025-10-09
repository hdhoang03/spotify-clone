package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.AlbumRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.entity.Album;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.AlbumMapper;
import com.spotify.spotify.repository.AlbumRepository;
import com.spotify.spotify.repository.ArtistRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AlbumService {
    AlbumRepository albumRepository;
    ArtistRepository artistRepository;
    AlbumMapper albumMapper;
    Cloudinary cloudinary;

    private static final String UPLOAD_DIR = "uploads/albums/";

    @PreAuthorize("hasRole('ADMIN')")
    public AlbumResponse createAlbum(AlbumRequest request){
        if(albumRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.ALBUM_ALREADY_EXISTS);
        }

        Album album = albumMapper.toAlbum(request);
//        String avatarPath = saveFile(request.getAvatarUrl());
        String avatarPath = saveFileCloud(request.getAvatarUrl());
        album.setAlbumUrl(avatarPath);

        Artist artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        album.setArtists(Set.of(artist));
        album = albumRepository.save(album);
        return albumMapper.toAlbumResponse(album);
    }

    public AlbumResponse getAlbumById(String id){
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));
        return albumMapper.toAlbumResponse(album);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AlbumResponse updateAlbum(String id, AlbumRequest request){
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));

        albumMapper.updateAlbum(album, request);
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()){
//            String avatarPath = saveFile(request.getAvatarUrl());
            String avatarPath = saveFileCloud(request.getAvatarUrl());
            album.setAlbumUrl(avatarPath);
        }
        if (request.getArtistId() != null){
            Artist artist = artistRepository.findById(request.getArtistId())
                    .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
            album.setArtists(Set.of(artist));
        }
        album = albumRepository.save(album);
        return albumMapper.toAlbumResponse(album);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAlbum(String id){
        if(!albumRepository.existsById(id)){
            throw new AppException(ErrorCode.ALBUM_NOT_FOUND);
        }
        albumRepository.deleteById(id);
    }

    public List<AlbumResponse> searchAlbum(String keyword){
        return albumRepository.findByNameContaining(keyword)
                .stream()
                .map(albumMapper::toAlbumResponse)
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

    private String saveFile(MultipartFile file){
        if(file == null || file.isEmpty()) return null;
        try {
            Path dirPath = Paths.get(UPLOAD_DIR);
            if(!Files.exists(dirPath)){
                Files.createDirectories(dirPath);
            }
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            return "/" + UPLOAD_DIR + filename;
        } catch (IOException e){
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}