package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.*;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.SongMapper;
import com.spotify.spotify.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        Song song = songMapper.toSong(request);
        Album album = albumRepository.findById(request.getAlbumId())
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));

//        String coverPath = saveFile(request.getCoverUrl(), "covers");
//        String audioPath = saveFile(request.getAudioUrl(), "audios");
        String coverPath = saveFileCloud(request.getCoverUrl(), "covers");
        String audioPath = saveFileCloud(request.getAudioUrl(), "audios");

        //set thủ công vì trong mapping ignore
        song.setAlbum(album);
        song.setArtist(artist);
        song.setCoverUrl(coverPath);
        song.setAudioUrl(audioPath);
        song.setUploadedBy(user);
        song.setCreatedAt(LocalDateTime.now());

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

        if(request.getGenre() != null && !request.getGenre().isEmpty()){
            Category category = categoryRepository.findById(request.getGenre())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            song.setCategory(category);
        }

        if(request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            String coverPath = saveFileCloud(request.getCoverUrl(), "covers");
            song.setCoverUrl(coverPath);
        }

        if (request.getAudioUrl() != null && !request.getAudioUrl().isEmpty()){
            String audioPath = saveFileCloud(request.getAudioUrl(), "audios");
            song.setAudioUrl(audioPath);
        }

        song = songRepository.save(song);
        return songMapper.toSongResponse(song);
    }

    public List<SongResponse> searchSongsByTitle(String keyword){
        return songRepository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(songMapper::toSongResponse)
                .toList();
    }

    public List<SongResponse> getAllSongs(){
        return songRepository.findAll().stream()
                .map(songMapper::toSongResponse)
                .toList();
    }

    public List<SongResponse> getSongsByAlbum(String albumId){
        return songRepository.findByAlbum_Id(albumId).stream()
                .map(songMapper::toSongResponse)
                .toList();
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
        if(!songRepository.existsById(id)){
            throw new AppException(ErrorCode.SONG_NOT_FOUND);
        }
        songRepository.deleteById(id);
    }

    private String saveFileCloud(MultipartFile file, String folder){ //cloudinary
        if(file == null || file.isEmpty()) return null;
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto" //Cho phép up ảnh, mp3 và mp4
                    )
            );
            return uploadResult.get("secure_url").toString(); //Trả về URL Public
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

    public List<SongResponse> searchSongs(String keyword, String artist, String category, Integer year){
        Specification<Song> spec = Specification.allOf();

        if (keyword != null && !keyword.isEmpty()){
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
        }

        if (artist != null && !artist.isEmpty()){
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("artist").get("name")), "%" + artist.toLowerCase() + "%"));
        }

        if (category != null && !category.isEmpty()){
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("category").get("name")), "%" + category.toLowerCase() + "%"));
        }

        if (year != null){
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.function("YEAR", Integer.class, root.get("releaseDate")), year));
        }

        return songRepository.findAll(spec)
                .stream()
                .map(songMapper::toSongResponse)
                .toList();
    }
}