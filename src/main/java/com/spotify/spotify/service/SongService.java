package com.spotify.spotify.service;

import com.spotify.spotify.configuration.RabbitMQConfig;
import com.spotify.spotify.constaint.NotificationTargetType;
import com.spotify.spotify.dto.event.SseNotificationEvent;
import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.CloudinaryResponse;
import com.spotify.spotify.dto.response.CustomPageImpl;
import com.spotify.spotify.dto.response.NotificationResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.*;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
//import com.spotify.spotify.kafka.KafkaProducerService;
import com.spotify.spotify.mapper.SongMapper;
import com.spotify.spotify.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.JoinType;

import java.time.LocalDateTime;
import java.util.*;

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
//    KafkaProducerService kafkaProducerService;
    RabbitMQProducerService rabbitMQProducerService;
    SongMapper songMapper;
    CloudinaryService cloudinaryService;

    private static final String UPLOAD_DIR = "uploads/";

    @CacheEvict(value = {"song_detail", "songs_page", "admin_songs_page", "songs_by_album", "songs_by_category", "songs_by_day", "get_songs_artist", "top_streamed_songs", "top_liked_songs"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SongResponse createSong(SongRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Song song = songMapper.toSong(request);

        Artist artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));

        Album album = null; //Khi tạo có thể gửi/không gửi albumId cũng được, có thể bỏ cũng không sao
        if (request.getAlbumId() != null && !request.getAlbumId().isEmpty()) {
            album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));
        }

        Category category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()){
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        if (request.getFeaturedArtistIds() != null && !request.getFeaturedArtistIds().isEmpty()){
            Set<Artist> featuredArtists = new HashSet<>(artistRepository.findAllById(request.getFeaturedArtistIds()));
            song.setFeaturedArtists(featuredArtists);
        }

        CloudinaryResponse coverPath = cloudinaryService.uploadFile(request.getCoverUrl(), "covers");
        CloudinaryResponse audioPath = cloudinaryService.uploadFile(request.getAudioUrl(), "audios");

        //set thủ công vì trong mapping ignore
        song.setAlbum(album);
        song.setCategory(category);
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
        //Hàm bắn thông báo khi có bài hát mới
        try {
//            this.sendNewSongNotificationViaKafka(
            this.sendNewSongNotificationViaRabitMQ(
                    artist.getId(),
                    artist.getName(),
                    song.getTitle(),
                    song.getCoverUrl()
            );
        } catch (Exception e) {
            log.error("Error while sending song notification via Kafka ", e);
        }
        return songMapper.toSongResponse(song);
    }

    @CacheEvict(value = {"song_detail", "songs_page", "admin_songs_page", "songs_by_album", "songs_by_category", "songs_by_day", "get_songs_artist", "top_streamed_songs", "top_liked_songs"}, allEntries = true)
    @Transactional
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

        if(request.getCategoryId() != null && !request.getCategoryId().isEmpty()){
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            song.setCategory(category);
        }

        if(request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            if (song.getCoverUrl() != null) cloudinaryService.deleteFile(song.getCoverUrl(), "image");
            CloudinaryResponse coverPath = cloudinaryService.uploadFile(request.getCoverUrl(), "covers");
            if (coverPath != null) song.setCoverUrl(coverPath.getUrl());
        }

        if (request.getAudioUrl() != null && !request.getAudioUrl().isEmpty()){
            if (song.getAudioUrl() != null) cloudinaryService.deleteFile(song.getAudioUrl(), "video");
            CloudinaryResponse audioPath = cloudinaryService.uploadFile(request.getAudioUrl(), "audios");
            if (audioPath != null) {
                song.setAudioUrl(audioPath.getUrl());
                song.setDuration(audioPath.getDuration());
            }
        }

        if (request.getFeaturedArtistIds() != null) {
            Set<Artist> featuredArtists = new HashSet<>(artistRepository.findAllById(request.getFeaturedArtistIds()));
            song.setFeaturedArtists(featuredArtists);
        }

        song = songRepository.save(song);
        return songMapper.toSongResponse(song);
    }

    public Page<SongResponse> searchSongsByTitle(String keyword, Pageable pageable){
        return songRepository.searchActiveSongByTitle(keyword, pageable)
                .map(songMapper::toSongResponse);
    }

    @Cacheable(value = "songs_page", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<SongResponse> getAllSongs(Pageable pageable){
        Page<SongResponse> page = songRepository.findAllActiveSongs(pageable)
                .map(songMapper::toSongResponse);
        return new CustomPageImpl<>(page.getContent(), pageable, page.getTotalElements());
    }

    @Cacheable(value = "songs_by_album", key = "#albumId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SongResponse> getSongsByAlbum(String albumId, Pageable pageable){
        Page<SongResponse> page = songRepository.findByAlbum_Id(albumId, pageable)
                .map(songMapper::toSongResponse);
        return new CustomPageImpl<>(page.getContent(), pageable, page.getTotalElements());
    }

    @Cacheable(value = "songs_by_category", key = "#categoryId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SongResponse> getSongsByCategory(String categoryId, Pageable pageable){
        Page<SongResponse> page = songRepository.findByCategory_Id(categoryId, pageable)
                .map(songMapper::toSongResponse);
        return new CustomPageImpl<>(page.getContent(), pageable, page.getTotalElements());
    }

//    public List<SongResponse> getSongByArtist(String artistId){
//        return songRepository.findByArtist_Id(artistId).stream()
//                .map(songMapper::toSongResponse)
//                .toList();
//    }

    @Cacheable(value = "song_detail", key = "#id")
    public SongResponse getSong(String id){
        Song song = songRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
        return songMapper.toSongResponse(song);
    }

    @Cacheable(value = "songs_by_day")
    public List<SongResponse> getAllSongsByDay(){
        return songRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(songMapper::toSongResponse)
                .toList();
    }

    @Cacheable(value = "get_songs_artist", key = "#artistId")
    public List<SongResponse> getSongByArtist(String artistId){
        List<Song> songs = songRepository.findTopPopularSongsByArtist(artistId);
        return songs.stream()
                .map(songMapper::toSongResponse)
                .toList();
    }

    @CacheEvict(value = {"song_detail", "songs_page", "admin_songs_page", "songs_by_album", "songs_by_category", "songs_by_day", "get_songs_artist", "top_streamed_songs", "top_liked_songs"}, allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public void softDeleteSong(String id){
        Song song = songRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
        if (song.isDeleted()) return;
        song.setDeleted(true);
        songRepository.save(song);
    }

    @CacheEvict(value = {"song_detail", "songs_page", "admin_songs_page", "songs_by_album", "songs_by_category", "songs_by_day", "get_songs_artist", "top_streamed_songs", "top_liked_songs"}, allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreSong(String id){
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        if(!song.isDeleted()) return;
        song.setDeleted(false);
        songRepository.save(song);
    }

    @CacheEvict(value = {"song_detail", "songs_page", "admin_songs_page", "songs_by_album", "songs_by_category", "songs_by_day", "get_songs_artist", "top_streamed_songs", "top_liked_songs"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void forceDeleteSong(String id){
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        if (song.getAudioUrl() != null){
            cloudinaryService.deleteFile(song.getAudioUrl(), "video");
        }

        if (song.getCoverUrl() != null){
            cloudinaryService.deleteFile(song.getCoverUrl(), "image");
        }

         songRepository.delete(song);
    }

    //sendNewSongNotificationViaKafka
    public void sendNewSongNotificationViaRabitMQ(String artistId, String artistName, String songTitle, String coverUrl){
        NotificationResponse payload = NotificationResponse.builder()
                .id(UUID.randomUUID().toString())
                .type("NEW_SONG")
                .title("SpringTunes: Nhạc mới từ " + artistName)
                .message(artistName + " vừa phát hành nhạc mới " + songTitle)
                .targetUrl("/artist/" + artistId)
                .thumbnail(coverUrl)
                .createdAt(LocalDateTime.now())
                .build();

        SseNotificationEvent event = SseNotificationEvent.builder()
                .targetType(NotificationTargetType.ARTIST_FANS)
                .targetId(artistId)
                .notificationPayload(payload)
                .build();

//        kafkaProducerService.sendMessage("sse_topic", event);
        rabbitMQProducerService.sendMessage(RabbitMQConfig.SSE_QUEUE, event);
    }

    @Cacheable(value = "admin_songs_page", key = "#keyword + '_' + #artist + '_' + #category + '_' + #year + '_' + #isDeleted + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SongResponse> searchSongs(String keyword, String artist, String category, Integer year, boolean isDeleted, Pageable pageable){
        Specification<Song> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        spec = spec.and((root, query, cb) -> cb.equal(root.get("deleted"), isDeleted));

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

        Page<SongResponse> page = songRepository.findAll(spec, pageable)
                .map(songMapper::toSongResponse);
        return new CustomPageImpl<>(page.getContent(), pageable, page.getTotalElements());
    }
}