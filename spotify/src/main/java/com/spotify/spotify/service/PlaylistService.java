package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.entity.Playlist;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.PlaylistMapper;
import com.spotify.spotify.repository.PlaylistRepository;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Map;
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
    Cloudinary cloudinary;

    public PlaylistResponse createPlaylist(PlaylistRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Playlist playlist = playlistMapper.toPlaylist(request);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            String coverPath = saveFileCloud(request.getCoverUrl(), "spotify/playlists");
            playlist.setCoverUrl(coverPath);
        }
        if (playlist.getIsPublic() == null) playlist.setIsPublic(true); //không gửi param mặc định là true
        playlist.setUser(user);
        playlist.setSongs(new HashSet<>());

        playlist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(playlist);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(String playlistId, PlaylistUpdateRequest request){
        Playlist playlist = getPlayListAndCheckOwnership(playlistId);

        playlistMapper.updatePlaylist(playlist, request);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            if (playlist.getCoverUrl() != null){
                deleteFileCloud(playlist.getCoverUrl(), "image");
            }
            String coverUrl = saveFileCloud(request.getCoverUrl(), "spotify/playlists");
            playlist.setCoverUrl(coverUrl);
        }

        playlist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(playlist);
    }

    @Transactional
    public void deletePlaylist(String playlistId){
        Playlist playlist = getPlayListAndCheckOwnership(playlistId);

        if (playlist.getCoverUrl() != null){
            deleteFileCloud(playlist.getCoverUrl(), "image");
        }

        playlistRepository.delete(playlist);
    }

    public PlaylistResponse getPlaylist(String playlistId){
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAY_LIST_NOT_FOUND));

        if (!Boolean.TRUE.equals(playlist.getIsPublic())){
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null || !auth.isAuthenticated() || !playlist.getUser().getUsername().equals(auth.getName())){
                throw new AppException(ErrorCode.UNAUTHENTICATED);//Nếu isPublic = false thì không phải chủ không xem được
            }
        }
        log.info("Playlist {} createdAt = {}", playlistId, playlist.getCreatedAt());
        return playlistMapper.toPlaylistResponse(playlist);
    }

    @Transactional
    public void addSongToPlaylist(String playlistId, String songId){
        Playlist playlist = getPlayListAndCheckOwnership(playlistId);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
        if (playlist.getSongs().contains(song)){
            throw new AppException(ErrorCode.SONG_ALREADY_IN_PLAYLIST);
        }

        playlist.getSongs().add(song);
        playlistRepository.save(playlist);
    }

    @Transactional
    public void removeSongFromPlaylist(String playlistId, String songId){
        Playlist playlist = getPlayListAndCheckOwnership(playlistId);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        boolean removed = playlist.getSongs().remove(song);
        if (!removed){
            throw new AppException(ErrorCode.SONG_NOT_IN_PLAYLIST);
        }

        playlistRepository.save(playlist);
    }

    public Page<PlaylistResponse> getUserPublicPlaylists(String userId, Pageable pageable){
        if (!userRepository.existsById(userId)){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return playlistRepository.findByUserIdAndIsPublicTrue(userId, pageable)
                .map(playlistMapper::toPlaylistResponse);
    }

    public Page<PlaylistResponse> getMyPlaylists(Pageable pageable){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return playlistRepository.findByUserId(user.getId(), pageable)
                .map(playlistMapper::toPlaylistResponse);
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
