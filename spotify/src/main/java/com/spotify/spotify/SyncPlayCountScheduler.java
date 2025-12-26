package com.spotify.spotify;

import com.spotify.spotify.repository.SongRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SyncPlayCountScheduler {
    RedisTemplate<String, Object> redisTemplate;
    SongRepository songRepository;

    static String PLAY_BUFFER_KEY = "song_play_buffer";

    @Scheduled(fixedDelay = 60000) //60s chạy 1 lần
    @Transactional
    public void sysPlayCountsToDatabase(){
        Map<Object, Object> buffer = redisTemplate.opsForHash().entries(PLAY_BUFFER_KEY);

        if (buffer.isEmpty()) return;
        log.info("Start syncing {} songs play count to Database...", buffer.size());

        for (Map.Entry<Object, Object> entry : buffer.entrySet()){
            try {
                String songId = (String) entry.getKey();
                Long count = Long.valueOf(entry.getValue().toString());
                songRepository.incrementPlayCountAmount(songId, count);

                redisTemplate.opsForHash().delete(PLAY_BUFFER_KEY, songId);
            } catch (Exception e){
                log.error("Error syncing play count for song " + entry.getKey(), e);
            }
        }
        log.info("Sync play count completed.");
    }
}
