package com.spotify.spotify.service;

import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.ArtistMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArtistService {
    ArtistRepository artistRepository;
    ArtistMapper artistMapper;

    private static final String UPLOAD_DIR = "uploads/artists/";

    @PreAuthorize("hasRole('ADMIN')")
    public ArtistResponse createArtist(ArtistRequest request){
        Artist artist = artistMapper.toArtist(request);

        String avatarPath = saveFile(request.getAvatarUrl());
        artist.setAvatarUrl(avatarPath);
        artist = artistRepository.save(artist);
        return artistMapper.toArtistResponse(artist);
    }

    public List<ArtistResponse> getAllArtists(){
        return artistRepository.findAll()
                .stream()
                .map(artistMapper::toArtistResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ArtistResponse updateArtist(String id, ArtistRequest request){
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
        artistMapper.updateArtist(artist, request);
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()){
            String avatarPath = saveFile(request.getAvatarUrl());
            artist.setAvatarUrl(avatarPath);
        }
        artist = artistRepository.save(artist);
        return artistMapper.toArtistResponse(artist);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteArtist(String id){
        if(!artistRepository.existsById(id)){
            throw new AppException(ErrorCode.ARTIST_NOT_FOUND);
        }
        artistRepository.deleteById(id);
    }

    public ArtistResponse getArtistById(String id){
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
        return artistMapper.toArtistResponse(artist);
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
