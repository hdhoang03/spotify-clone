package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.CloudinaryResponse;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.dto.response.PlaylistSongResponse;
import com.spotify.spotify.entity.Playlist;
import com.spotify.spotify.entity.PlaylistSong;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.PlaylistMapper;
import com.spotify.spotify.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaylistService {
    UserRepository userRepository;
    PlaylistMapper playlistMapper;
    PlaylistRepository playlistRepository;
    SongRepository songRepository;
    CloudinaryService cloudinaryService;
    PlaylistSongRepository playlistSongRepository;

    @CacheEvict(value = {"playlist_detail", "playlist_songs", "user_playlists", "my_playlists"}, allEntries = true)
    public PlaylistResponse createPlaylist(PlaylistRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Playlist playlist = playlistMapper.toPlaylist(request);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getCoverUrl(), "spotify/playlists");
            if (cloudRes != null) playlist.setCoverUrl(cloudRes.getUrl());
        }

        if (playlist.getIsPublic() == null) playlist.setIsPublic(true); //không gửi param mặc định là true
        playlist.setUser(user);
        playlist.setPlaylistSongs(new HashSet<>());

        playlist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(playlist);
    }

    @CacheEvict(value = {"playlist_detail", "playlist_songs", "user_playlists", "my_playlists"}, allEntries = true)
    @Transactional
    public PlaylistResponse updatePlaylist(String playlistId, PlaylistUpdateRequest request){
        Playlist playlist = getPlayListAndCheckOwnership(playlistId);

        playlistMapper.updatePlaylist(playlist, request);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            if (playlist.getCoverUrl() != null){
                cloudinaryService.deleteFile(playlist.getCoverUrl(), "image");
            }
            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getCoverUrl(), "spotify/playlists");
            if (cloudRes != null) playlist.setCoverUrl(cloudRes.getUrl());
        }

        playlist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(playlist);
    }

//    @Transactional
//    public void createDefaultPlaylist(User user){
//        Playlist likedSongs = Playlist.builder()
//                .name("Liked Songs")
//                .description("Your favorite songs")
//                .user(user)
//                .isPublic(false)
//                .isDefault(true)
//                .playlistSongs(new HashSet<>())
//                .createdAt(LocalDateTime.now())
//                .build();
//        playlistRepository.save(likedSongs);
//    }

    @CacheEvict(value = {"playlist_detail", "playlist_songs", "user_playlists", "my_playlists"}, allEntries = true)
    @Transactional
    public void deletePlaylist(String playlistId){
        Playlist playlist = getPlayListAndCheckOwnership(playlistId);

        if (Boolean.TRUE.equals(playlist.getIsDefault())) {
            throw new AppException(ErrorCode.CANNOT_DELETE_DEFAULT_PLAYLIST);
        }

        if (playlist.getCoverUrl() != null){
            cloudinaryService.deleteFile(playlist.getCoverUrl(), "image");
        }

        playlistRepository.delete(playlist);
    }
    // Dùng 'unless' để chặn việc lưu vào cache nếu playlist trả về không phải là public
    @Cacheable(value = "playlist_detail", key = "#playlistId", unless = "!#result.isPublic")
    public PlaylistResponse getPlaylist(String playlistId){
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAY_LIST_NOT_FOUND));

        if (!Boolean.TRUE.equals(playlist.getIsPublic())){
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || !playlist.getUser().getUsername().equals(auth.getName())){
                throw new AppException(ErrorCode.UNAUTHENTICATED);//Nếu isPublic = false thì không phải chủ không xem được
            }
        }
        log.info("Playlist {} createdAt = {}", playlistId, playlist.getCreatedAt());
        return playlistMapper.toPlaylistResponse(playlist);
    }

    @CacheEvict(value = {"playlist_detail", "playlist_songs", "user_playlists", "my_playlists"}, allEntries = true)
    @Transactional
    public void addSongToPlaylist(String playlistId, String songId){
        Playlist playlist = getPlayListAndCheckOwnership(playlistId);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

//        if (playlist.getSongs().contains(song)){
//            throw new AppException(ErrorCode.SONG_ALREADY_IN_PLAYLIST);
//        }
//        playlist.getSongs().add(song);

        if (playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)){
            throw new AppException(ErrorCode.SONG_ALREADY_IN_PLAYLIST);
        }

        PlaylistSong playlistSong = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                // addedAt sẽ tự động sinh do có @PrePersist
                .build();

        playlistSongRepository.save(playlistSong);
    }

    @CacheEvict(value = {"playlist_detail", "playlist_songs", "user_playlists", "my_playlists"}, allEntries = true)
    @Transactional
    public void removeSongFromPlaylist(String playlistId, String songId){
        getPlayListAndCheckOwnership(playlistId);

        if (!playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)){
            throw new AppException(ErrorCode.SONG_NOT_IN_PLAYLIST);
        }

        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    @Cacheable(value = "playlist_songs", key = "#playlistId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PlaylistSongResponse> getPlaylistSongs(String playlistId, Pageable pageable){
        Page<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistIdOrderByAddedAtDesc(playlistId, pageable);
        return playlistSongs.map(ps -> {
            Song s = ps.getSong();

            return PlaylistSongResponse.builder()
                    .id(s.getId())
                    .title(s.getTitle())
                    .artist(s.getArtist() != null ? s.getArtist().getName() : "Unknown")
                    .artistId(s.getArtist().getId())
                    .albumName(s.getAlbum() != null ? s.getAlbum().getName() : null)
                    .coverUrl(s.getCoverUrl())
                    .audioUrl(s.getAudioUrl())
                    .duration(s.getDuration())
                    .addedAt(ps.getAddedAt())
                    .build();
        });
    }

    @Cacheable(value = "user_playlists", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PlaylistResponse> getUserPublicPlaylists(String userId, Pageable pageable){
        if (!userRepository.existsById(userId)){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return playlistRepository.findPublicPlaylistsWithCount(userId, pageable);
    }

    @Cacheable(value = "my_playlists", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PlaylistResponse> getMyPlaylists(Pageable pageable){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return playlistRepository.findMyPlaylistsWithCount(user.getId(), pageable);
    }

    private Playlist getPlayListAndCheckOwnership(String playlistId){
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAY_LIST_NOT_FOUND));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!playlist.getUser().getUsername().equals(username)){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return playlist;
    }

    /*Nếu Chủ sở hữu gọi API này trước, Redis sẽ lưu nguyên một list gồm cả Private + Public.
    Lúc sau người ngoài ấn vào tường của Chủ sở hữu, Redis nhả thẳng cái list đó ra -> Lộ toàn bộ Playlist ẩn.*/
//    @Cacheable(value = "user_playlists", key = "#targetUserId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PlaylistResponse> getUserPlaylists(String targetUserId, Pageable pageable){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean isOwner = targetUser.getUsername().equals(username);

        if (isOwner){
            return playlistRepository.findByUser_Id(targetUserId, pageable)
                    .map(playlistMapper::toPlaylistResponse);
        } else {
            return playlistRepository.findByUser_IdAndIsPublicTrue(targetUserId, pageable)
                    .map(playlistMapper::toPlaylistResponse);
        }
    }
}
