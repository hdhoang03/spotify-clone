package com.spotify.spotify.service;

import com.spotify.spotify.dto.request.SongStreamRequest;
import com.spotify.spotify.dto.response.SongStreamResponse;
import com.spotify.spotify.dto.response.StreamStatResponse;
import com.spotify.spotify.dto.response.TopStreamResponse;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.SongStream;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.SongStreamMapper;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.repository.SongStreamRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SongStreamService {
    SongStreamMapper songStreamMapper;
    SongStreamRepository songStreamRepository;
    SongRepository songRepository;
    UserRepository userRepository;

    @Transactional //Query là phải có transactional
    public void increasePlayCount(String songId){ //Tăng lượt play_count của bài hát
        if (!songRepository.existsById(songId)){
            throw new AppException(ErrorCode.SONG_NOT_FOUND);
        }
        songRepository.incrementPlayCount(songId);
    }

    @Transactional //Toàn vẹn dữ liệu khi save
    public SongStreamResponse createStream(SongStreamRequest request){ //Tạo 1 lượt stream nếu nghe bài hát trên 30 với userid và songid đó
        //Valid logic 30s
        if (request.getDuration() != null && request.getDuration() < 30){
            throw new AppException(ErrorCode.STREAM_TOO_SHORT);//return null cũng được
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Song song = songRepository.findById(request.getSongId()).orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
//        songRepository.incrementPlayCount(song.getId());//Nếu muốn tăng playCount không liên quan đến lượt stream thì dùng cách này

        //Kiểm tra thời gian cooldown 60s để tránh spam
        List<SongStream> recent = songStreamRepository
                .findRecentStreams(user.getId(), song.getId(), PageRequest.of(0,1));
        if (!recent.isEmpty()){
            LocalDateTime last = recent.get(0).getCreatedAt();
            long seconds = Duration.between(last, LocalDateTime.now()).getSeconds();
            if (seconds < 60){
                return songStreamMapper.toSongStreamResponse(recent.get(0));
            }
        }

        SongStream stream = songStreamMapper.toSongStream(request);
        //Map thủ công vì trong mapper ignore
        stream.setCreatedAt(LocalDateTime.now());
        stream.setUser(user);
        stream.setSong(song);

        if (stream.getDuration() == null){
            stream.setDuration(30L);//30s
        }
        log.debug("User {} streamed song {} length {}", user.getId(), song.getId(), stream.getDuration());
        return songStreamMapper.toSongStreamResponse(songStreamRepository.save(stream));
    }

    public Long countSongStream(String songId){ //Đếm số lượt stream của bài hát đó
        return songStreamRepository.countBySong_Id(songId);
    }

    public Page<SongStreamResponse> getMyStreams(Pageable pageable){ //Lấy lịch sử nghe nhạc của chính user đó
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return songStreamRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(songStreamMapper::toSongStreamResponse);
    }

    public Boolean hasUserStreamedSong(String songId){ //Kiểm tra user có stream bài hát không?
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return songStreamRepository.existsByUser_IdAndSong_Id(user.getId(), songId);
    }

    public List<StreamStatResponse> getStreamStats(String songId, LocalDate start, LocalDate end){ //Biểu đồ thống kê lượt stream
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        return songStreamRepository.getStreamStats(songId, startDateTime, endDateTime);
    }

    public List<TopStreamResponse> getTopStreamSongs(){ //Top bài hát nghe nhiều
        return songStreamRepository.findTopStreamSongs();
    }
}




