package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.entity.Playlist;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.PlaylistMapper;
import com.spotify.spotify.repository.PlaylistRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaylistService {
    UserRepository userRepository;
    PlaylistMapper playlistMapper;
    PlaylistRepository playlistRepository;
    Cloudinary cloudinary;

    public PlaylistResponse createPlaylist(PlaylistRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Playlist playlist = playlistMapper.toPlaylistRequest(request);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            String coverPath = saveFileCloud(request.getCoverUrl(), "covers");
            playlist.setCoverUrl(coverPath);
        }
        playlist.setIsPublic(true);
        playlist.setUser(user);
        playlist.setSongs(new HashSet<>());

        playlist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(playlist);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(String playlistId, PlaylistUpdateRequest request){
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAY_LIST_NOT_FOUND));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!playlist.getUser().getId().equals(user.getId())){ //Kiểm tra playlist có thuộc về user đó không
            throw new AppException(ErrorCode.PLAY_LIST_NOT_FOUND);
        }

        playlistMapper.updatePlaylist(playlist, request);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()){
            String coverUrl = saveFileCloud(request.getCoverUrl(), "covers");
            playlist.setCoverUrl(coverUrl);
        }

        playlist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(playlist);
    }

    @Transactional
    public void deletePlaylist(String playlistId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAY_LIST_NOT_FOUND));

        if (!playlist.getUser().getId().equals(user.getId())) { //Xác thực người dùng
            throw new AppException(ErrorCode.PLAY_LIST_NOT_FOUND);
        }

        playlistRepository.delete(playlist);
    }

    public PlaylistResponse getPlaylist(String playlistId){
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAY_LIST_NOT_FOUND));

        if (!playlist.getIsPublic()){
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!playlist.getUser().getUsername().equals(username)){
                throw new AppException(ErrorCode.UNAUTHENTICATED);//Nếu isPublic = false thì không phải chủ không xem được
            }
        }
        log.info("Playlist {} createdAt = {}", playlistId, playlist.getCreatedAt());
        return playlistMapper.toPlaylistResponse(playlist);
    }

    private String saveFileCloud(MultipartFile file, String folder){
        if (file == null || file.isEmpty()) return null;
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
}
