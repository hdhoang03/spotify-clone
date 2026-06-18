package com.spotify.spotify.service;

import com.spotify.spotify.dto.response.LikeSongResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.dto.response.TopLikeSongResponse;
import com.spotify.spotify.entity.LikeSong;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.LikeSongMapper;
import com.spotify.spotify.repository.LikeSongRepository;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LikeSongService {
    LikeSongRepository likeSongRepository;
    LikeSongMapper likeSongMapper;
    UserRepository userRepository;
    SongRepository songRepository;

    public Boolean hasLiked(String songId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return likeSongRepository.existsByUser_IdAndSong_Id(user.getId(), songId);
    }

    public Long countSongLikes(String songId){
        return likeSongRepository.countBySong_Id(songId);
    }

    @Cacheable(value = "my_song", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<LikeSongResponse> getMyLikedSongs(Pageable pageable){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return likeSongRepository.findAllByUserIdFetchSong(user.getId(), pageable)
                .map(likeSongMapper::toLikeSongResponse);
    }

    @CacheEvict(value = "my_song", allEntries = true)
    @Transactional
    public boolean toggleLike(String songId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!songRepository.existsById(songId)){
            throw new AppException(ErrorCode.SONG_NOT_FOUND);
        }

        if (likeSongRepository.existsByUser_IdAndSong_Id(user.getId(), songId)){
            likeSongRepository.deleteByUser_IdAndSong_Id(user.getId(), songId);
            songRepository.decrementLikeCount(songId);
            return false; //trả về false -> unlike
        } else {
            Song song = songRepository.getReferenceById(songId);
            likeSongRepository.save(new LikeSong(user, song));
            songRepository.incrementLikeCount(songId);
            return true; //trả về true -> like
        }
    }

    @Cacheable(value = "top_liked_songs", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<TopLikeSongResponse> getTopLikedSongs(Pageable pageable){
        return likeSongRepository.findTopLikedSongs(pageable);
    }
}
