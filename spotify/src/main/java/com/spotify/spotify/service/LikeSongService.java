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
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
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

    public List<LikeSongResponse> getMyLikedSongs(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return likeSongRepository.findAllByUserIdFetchSong(user.getId())
                .stream()
                .map(likeSongMapper::toLikeSongResponse)
                .toList();
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

//        LikeSong like = LikeSong.builder()
//                .user(user)
//                .song(song)
//                .build();
//        return likeSongMapper.toLikeSongResponse(likeSongRepository.save(like));

        LikeSong like = new LikeSong(user, song);//Hạn chế buider vì tốn tài nguyên
        likeSongRepository.save(like);

        song.setLikeCount(song.getLikeCount() + 1);
        songRepository.save(song);
    }

    @Transactional //delete phải có transactional mới hoạt động
    public void unlikeSong(String songId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        if (!likeSongRepository.existsByUser_IdAndSong_Id(user.getId(), songId)){
            throw new AppException(ErrorCode.NOT_LIKED_YET);
        }

        likeSongRepository.deleteByUser_IdAndSong_Id(user.getId(), songId);

        Long current = song.getLikeCount() == null ? 0L : song.getLikeCount();
        song.setLikeCount(current - 1);
        songRepository.save(song);
    }

    public List<TopLikeSongResponse> getTopLikedSongs(){
        return likeSongRepository.findTopLikedSongs();
    }
}
