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

    public Page<LikeSongResponse> getMyLikedSongs(Pageable pageable){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return likeSongRepository.findAllByUserIdFetchSong(user.getId(), pageable)
                .map(likeSongMapper::toLikeSongResponse);
    }

    @Transactional
    public void likeSong(String songId){ //LikeSongResponse nếu dùng phải trả về buider likeSong
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (likeSongRepository.existsByUser_IdAndSong_Id(user.getId(), songId)){
            throw new AppException(ErrorCode.ALREADY_LIKED);
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        LikeSong like = new LikeSong(user, song);//Hạn chế buider vì tốn tài nguyên
        likeSongRepository.save(like);

//        song.setLikeCount(song.getLikeCount() + 1); //Nếu viết như vậy sẽ gây lỗi race condition (nhiều người ấn like cùng 1 lần)
//        songRepository.save(song);

        songRepository.incrementLikeCount(songId);
    }

    @Transactional //delete phải có transactional mới hoạt động
    public void unlikeSong(String songId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!songRepository.existsById(songId)){ //Kiểm tra bài hát có tồn tại không
            throw new AppException(ErrorCode.SONG_NOT_FOUND);
        }

        if (!likeSongRepository.existsByUser_IdAndSong_Id(user.getId(), songId)){ //Kiểm tra đã like chứa
            throw new AppException(ErrorCode.NOT_LIKED_YET);
        }

        likeSongRepository.deleteByUser_IdAndSong_Id(user.getId(), songId);

        songRepository.decrementLikeCount(songId);
    }

    public Page<TopLikeSongResponse> getTopLikedSongs(Pageable pageable){
        return likeSongRepository.findTopLikedSongs(pageable);
    }
}
