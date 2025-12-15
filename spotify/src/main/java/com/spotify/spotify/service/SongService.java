package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.CloudinaryResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.*;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.SongMapper;
import com.spotify.spotify.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.JoinType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SongService {
    UserRepository userRepository;
    SongRepository songRepository;
    AlbumRepository albumRepository;
    ArtistRepository artistRepository;
    CategoryRepository categoryRepository;
    SongMapper songMapper;
    Cloudinary cloudinary;

    private static final String UPLOAD_DIR = "uploads/";

    @PreAuthorize("hasRole('ADMIN')")
    public SongResponse createSong(SongRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Artist artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        Album album = null; //Khi tạo có thể gửi/không gửi albumId cũng được, có thể bỏ cũng không sao
        if (request.getAlbumId() != null && !request.getAlbumId().isEmpty()) {
            album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));
        }

        Song song = songMapper.toSong(request);
//        String coverPath = saveFile(request.getCoverUrl(), "covers");
//        String audioPath = saveFile(request.getAudioUrl(), "audios");
        CloudinaryResponse coverPath = saveFileCloud(request.getCoverUrl(), "covers");
        CloudinaryResponse audioPath = saveFileCloud(request.getAudioUrl(), "audios");

        //set thủ công vì trong mapping ignore
        song.setAlbum(album);
        song.setArtist(artist);
        song.setUploadedBy(user);
        song.setCreatedAt(LocalDateTime.now());

        if (coverPath != null){
            song.setCoverUrl(coverPath.getUrl());
        }

        if (audioPath != null){
            song.setAudioUrl(audioPath.getUrl());
            song.setDuration(audioPath.getDuration());
        }

        song = songRepository.save(song);
        return songMapper.toSongResponse(song);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public SongResponse updateSong(String id, SongRequest request){
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
        songMapper.updateSong(song, request);

        if(request.getArtistId() != null && !request.getArtistId().isEmpty()){
            Artist artist = artistRepository.findById(request.getArtistId())
                    .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
            song.setArtist(artist);
        }

        if(request.getAlbumId() != null && !request.getAlbumId().isEmpty()){
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));
            song.setAlbum(album);
        }
//
//        if(request.getCategory() != null && !request.getCategory().isEmpty()){
//            Category category = categoryRepository.findById(request.getCategory())
//                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
//            song.setCategory(category);
//        }

        if(request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            deleteFileCloud(song.getCoverUrl(), "image");
            CloudinaryResponse coverPath = saveFileCloud(request.getCoverUrl(), "covers");
            song.setCoverUrl(coverPath.getUrl());
        }

        if (request.getAudioUrl() != null && !request.getAudioUrl().isEmpty()){
            deleteFileCloud(song.getAudioUrl(), "video");
            CloudinaryResponse audioPath = saveFileCloud(request.getAudioUrl(), "audios");
            song.setAudioUrl(audioPath.getUrl());
            song.setDuration(audioPath.getDuration());
        }

        song = songRepository.save(song);
        return songMapper.toSongResponse(song);
    }

    public Page<SongResponse> searchSongsByTitle(String keyword, Pageable pageable){
        return songRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(keyword, pageable)
                .map(songMapper::toSongResponse);
    }

    public List<SongResponse> getAllSongs(){
        return songRepository.findAll().stream()
                .map(songMapper::toSongResponse)
                .toList();
    }

    public Page<SongResponse> getSongsByAlbum(String albumId, Pageable pageable){
        return songRepository.findByAlbum_Id(albumId, pageable)
                .map(songMapper::toSongResponse);
    }

    public List<SongResponse> getSongByArtist(String artistId){
        return songRepository.findByArtist_Id(artistId).stream()
                .map(songMapper::toSongResponse)
                .toList();
    }

    public SongResponse getSong(String id){
        Song song = songRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
        return songMapper.toSongResponse(song);
    }

    public List<SongResponse> getAllSongsByDay(){
        return songRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(songMapper::toSongResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSong(String id){
        Song song = songRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
        songRepository.delete(song);
        deleteFileCloud(song.getAudioUrl(), "video");
        deleteFileCloud(song.getCoverUrl(), "image");
    }

    public Page<SongResponse> searchSongs(String keyword, String artist, String category, Integer year, Pageable pageable){
        Specification<Song> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (keyword != null && !keyword.isEmpty()){
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
        }

        if (artist != null && !artist.isEmpty()){
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("artist", JoinType.LEFT).get("name")), "%" + artist.toLowerCase() + "%"));
        }

        if (category != null && !category.isEmpty()){
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("category", JoinType.LEFT).get("name")), "%" + category.toLowerCase() + "%"));
        }

        if (year != null){
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.function("YEAR", Integer.class, root.get("releaseDate")), year));
        }

        return songRepository.findAll(spec, pageable)
                .map(songMapper::toSongResponse);
    }

    private CloudinaryResponse saveFileCloud(MultipartFile file, String folder) { //cloudinary
        if (file == null || file.isEmpty()) return null;

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto" //Cho phép up ảnh, mp3 và mp4
                    )
            );
            String url = (String) uploadResult.get("secure_url");
            Object durationObj = uploadResult.get("duration");
            Double duration = 0.0;

            if (durationObj != null) {
                if (durationObj instanceof Double) {
                    duration = (Double) durationObj;
                } else if (durationObj instanceof Integer) {
                    duration = ((Integer) durationObj).doubleValue();
                }
            }
            return CloudinaryResponse.builder()
                    .url(url)
                    .duration(duration)
                    .build();
        } catch (Exception e){
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String saveFile(MultipartFile file, String folder){ //local
        if(file == null || file.isEmpty()) return null;
        try {
            Path dirPath = Paths.get(UPLOAD_DIR + folder);
            if (!Files.exists(dirPath)){
                Files.createDirectories(dirPath);
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            return "/" + UPLOAD_DIR + folder + "/" + filename;
        } catch (IOException e){
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
            }catch (IOException e){
                log.error("Failed to delete file on Cloudinary: {}", publicId);
            }
        }
    }
}