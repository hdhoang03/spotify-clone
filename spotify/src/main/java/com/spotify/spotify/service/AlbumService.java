package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.AlbumRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Album;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.AlbumMapper;
import com.spotify.spotify.mapper.SongMapper;
import com.spotify.spotify.repository.AlbumRepository;
import com.spotify.spotify.repository.ArtistRepository;
import com.spotify.spotify.repository.SongRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AlbumService {
    AlbumRepository albumRepository;
    ArtistRepository artistRepository;
    SongRepository songRepository;
    AlbumMapper albumMapper;
    Cloudinary cloudinary;
    SongMapper songMapper;

    private static final String UPLOAD_DIR = "uploads/albums/";

    @PreAuthorize("hasRole('ADMIN')")
    public AlbumResponse createAlbum(AlbumRequest request){
        if(albumRepository.existsByNameAndArtists_Id(request.getName(), request.getArtistId())){
            throw new AppException(ErrorCode.ALBUM_ALREADY_EXISTS);
        }

        Album album = albumMapper.toAlbum(request);
        String avatarPath = saveFileCloud(request.getAvatarUrl(), "spotify/albums"); //"spotify/albums"
        album.setAlbumUrl(avatarPath);

        Artist artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        album.setArtists(Set.of(artist));
        album = albumRepository.save(album);
        return albumMapper.toAlbumResponse(album);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void addSongsToAlbum(String albumId, List<String> songIds){
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));

        List<Song> songs = songRepository.findAllById(songIds);
        if (songs.size() != songIds.size()){
            throw new AppException(ErrorCode.SONG_NOT_FOUND);
        }

        Set<String> albumArtistIds = album.getArtists().stream()
                .map(Artist::getId)
                .collect(Collectors.toSet());

        //Kiểm tra bài hát phải cùng 1 ca sĩ mới thêm vào 1 album được
        for (Song song : songs){
            String songArtistId = song.getArtist().getId();
            if (!albumArtistIds.contains(songArtistId)){ //Kiểm tra nghệ sĩ có nằm trong danh sách nghệ sĩ của album không?
                throw new AppException(ErrorCode.SONG_ARTIST_MISMATCH);
            }

            if (song.getAlbum() != null){
                if (song.getAlbum().getId().equals(albumId)){
                    throw new AppException(ErrorCode.SONG_ALREADY_IN_PLAYLIST);
                } else {
                    throw new AppException(ErrorCode.SONG_BELONGS_TO_ANOTHER_ALBUM);
                }
            }

            song.setAlbum(album);
        }
        songRepository.saveAll(songs);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void removeSongFromAlbum(String albumId, String songId){
        albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        if (song.getAlbum() == null || !song.getAlbum().getId().equals(albumId)){
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        song.setAlbum(null);
        songRepository.save(song);
    }

    public Page<SongResponse> getAllSongsFromAlbum(String albumId, Pageable pageable){
        if(!albumRepository.existsById(albumId)){
            throw new AppException(ErrorCode.ALBUM_NOT_FOUND);
        }

        return songRepository.findByAlbum_Id(albumId, pageable)
                .map(songMapper::toSongResponse);
    }

    public List<AlbumResponse> getAlbumsByArtist(String artistId){
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        List<Album> albums = albumRepository.findByArtists_Id(artistId);

        if(albums.isEmpty()) return Collections.emptyList();

        return albums.stream()
                .map(albumMapper::toAlbumResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AlbumResponse> getAllAlbum(Pageable pageable){
        return albumRepository.findAll(pageable)
                .map(albumMapper::toAlbumSummary);//chỉ lấy album không lấy các bài hát đi kèm
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
            deleteFileCloud(album.getAlbumUrl(), "image");
            String avatarPath = saveFileCloud(request.getAvatarUrl(), "spotify/albums");
            album.setAlbumUrl(avatarPath);
        }
        if (request.getArtistId() != null){
            Artist artist = artistRepository.findById(request.getArtistId())
                    .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
            album.setArtists(new HashSet<>(Set.of(artist)));
        }
        album = albumRepository.save(album);
        return albumMapper.toAlbumResponse(album);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAlbum(String id){
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));

        if (album.getSongs() != null){
            album.getSongs().forEach(song -> song.setAlbum(null));
        }

        if (album.getAlbumUrl() != null){
            deleteFileCloud(album.getAlbumUrl(), "image");
        }

        albumRepository.delete(album);
    }

    public Page<AlbumResponse> searchAlbum(String keyword, boolean isDeleted, Pageable pageable){
        Page<AlbumRepository.AlbumWithSongCount> projections = albumRepository.searchAlbumsWithCount(keyword, isDeleted, pageable);
        return projections.map(projection -> {
            Album album = projection.getAlbum();
            AlbumResponse response = albumMapper.toAlbumResponse(album);

            response.setSongCount(projection.getSongCount() != null ? projection.getSongCount().intValue() : 0);

            if (album.getArtists() != null && !album.getArtists().isEmpty()){
                String names = album.getArtists().stream()
                        .map(Artist::getName)
                        .collect(Collectors.joining(", "));
                response.setArtistName(names);
            }

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