package com.spotify.spotify.service;

import com.spotify.spotify.dto.request.SongStreamRequest;
import com.spotify.spotify.dto.response.*;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.SongStream;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.ArtistMapper;
import com.spotify.spotify.mapper.SongStreamMapper;
import com.spotify.spotify.repository.SongRepository;
import com.spotify.spotify.repository.SongStreamRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SongStreamService {
    SongStreamMapper songStreamMapper;
    SongStreamRepository songStreamRepository;
    SongRepository songRepository;
    UserRepository userRepository;
    RedisTemplate<String, Object> redisTemplate;
    ArtistMapper artistMapper;

    static String PLAY_COOLDOWN_KEY_FORMAT = "play_cooldown:%s:%s";
    static String STREAM_COOLDOWN_KEY_FORMAT = "stream_cooldown:%s:%s";

    @Transactional //Query là phải có transactional
    public void increasePlayCount(String songId){ //Tăng lượt play_count của bài hát
        if (!songRepository.existsById(songId)){
            throw new AppException(ErrorCode.SONG_NOT_FOUND);
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cooldownKey = String.format(PLAY_COOLDOWN_KEY_FORMAT, songId, username);
        Boolean isValidClick = redisTemplate.opsForValue()
                        .setIfAbsent(cooldownKey, "1", 10, TimeUnit.SECONDS);

        if(Boolean.TRUE.equals(isValidClick)){
            songRepository.incrementPlayCount(songId);
            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            SongStream playLog = SongStream.builder()
                    .song(song)
                    .user(user)
                    .duration(0L)
                    .validStream(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            songStreamRepository.save(playLog);
//            kafkaProducerService.sendMessage("play_count", songId);
//            log.info("Buffered play count for song: {}", songId);
//        } else {
//            log.debug("Spm click detected from user {} on song {}", username, songId);
        }

//        songRepository.incrementPlayCount(songId);
        //Việc tăng view bây giờ do Consumer làm, Service này chỉ bắn tin rồi thôi.
        //Nếu để dòng này lại, User sẽ phải chờ DB update xong mới nhận được phản hồi -> Chậm.
    }

//    @Transactional //Toàn vẹn dữ liệu khi save
//    public SongStreamResponse createStream(SongStreamRequest request){ //Tạo 1 lượt stream nếu nghe bài hát trên 30 với userid và songid đó
//        double currentDuration = request.getDuration() * request.getSpeed();
//        long minRealTimeSeconds = 20L; //Định nghĩa thời gian sàn tối thiểu (để chống tool click quá nhanh)
//
//        if (request.getDuration() != null && currentDuration < 30){//Valid logic 30s
//            throw new AppException(ErrorCode.STREAM_TOO_SHORT);//return null cũng được
//        }
//        if (request.getDuration() < minRealTimeSeconds){
//            throw new AppException(ErrorCode.STREAM_TOO_FAST_DETECTED);
//        }
//
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        Song song = songRepository.findById(request.getSongId()).orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
////        songRepository.incrementPlayCount(song.getId());//Nếu muốn tăng playCount không liên quan đến lượt stream thì dùng cách này
//
//        //Kiểm tra thời gian cooldown 60s để tránh spam
//        List<SongStream> recent = songStreamRepository
//                .findRecentStreams(user.getId(), song.getId(), PageRequest.of(0,1));
//        if (!recent.isEmpty()){
//            LocalDateTime last = recent.get(0).getCreatedAt();
//            long seconds = Duration.between(last, LocalDateTime.now()).getSeconds();
//            if (seconds < 60){
//                return songStreamMapper.toSongStreamResponse(recent.get(0));
//            }
//        }
//
//        SongStream stream = songStreamMapper.toSongStream(request);
//        //Map thủ công vì trong mapper ignore
//        stream.setCreatedAt(LocalDateTime.now());
//        stream.setUser(user);
//        stream.setSong(song);
//
//        if (stream.getDuration() == null){
//            stream.setDuration(request.getDuration());
//        }
//        log.debug("User {} streamed song {} length {}", user.getId(), song.getId(), stream.getDuration());
//        return songStreamMapper.toSongStreamResponse(songStreamRepository.save(stream));
//    }

    public long getValidStreamCount(String songId){
        return songStreamRepository.countBySongIdAndValidStreamTrue(songId);
    }

    @Transactional //10/05
    public SongStreamResponse createStream(SongStreamRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String songId = request.getSongId();

        String streamKey = String.format(STREAM_COOLDOWN_KEY_FORMAT, songId, username);
        Boolean isAllowedStream = redisTemplate.opsForValue().setIfAbsent(streamKey, "1", 30, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isAllowedStream)) {
            log.warn("User {} is attempting to stream too frequently for song {}", username, songId);
            return null;
        }

        if (request.getDuration() < 30){
            throw new AppException(ErrorCode.STREAM_TOO_SHORT);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        songRepository.incrementStreamCount(songId);

        SongStream stream = songStreamMapper.toSongStream(request);
        stream.setUser(user);
        stream.setSong(song);
        stream.setValidStream(true);
        stream.setCreatedAt(LocalDateTime.now());

        return songStreamMapper.toSongStreamResponse(songStreamRepository.save(stream));
    }

    public Long countSongStream(String songId){ //Đếm số lượt stream của bài hát đó
        return songStreamRepository.countBySongId(songId);
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

    @Cacheable(value = "top_streamed_songs")
    public List<TopStreamResponse> getTopStreamSongs(){ //Top bài hát nghe nhiều
        return songStreamRepository.findTopStreamSongs();
    }

    public List<TopLikeSongResponse> getMyTopTracksOfThisMonth(int month, int year){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return songStreamRepository.findMyTopTracksOfThisMonth(user.getId(), month, year);
    }

    public List<ArtistResponse> getMyTopArtistsOfMonth(int month, int year){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Artist> topArtists = songStreamRepository.findMyTopArtistsOfThisMonthEntity(user.getId(), month, year);

        return topArtists.stream()
                .map(artistMapper::toArtistResponse)
                .toList();
    }
}




