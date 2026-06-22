package com.spotify.spotify.service;

import com.spotify.spotify.dto.response.LyricResponse;
import com.spotify.spotify.entity.Lyrics;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.LyricMapper;
import com.spotify.spotify.repository.LyricsRepository;
import com.spotify.spotify.repository.SongRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LyricsService {
    LyricsRepository lyricsRepository;
    SongRepository songRepository;
    LyricMapper lyricMapper;

    @Cacheable(value = "lyrics", key = "#songId")
    public LyricResponse getLyricsBySongId(String songId) {
        Lyrics lyrics = lyricsRepository.findBySongId(songId)
                .orElseThrow(() -> new AppException(ErrorCode.LYRICS_NOT_FOUND));

        return lyricMapper.toLyricResponse(lyrics);
    }

    @CacheEvict(value = "lyrics", key = "#songId")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public LyricResponse saveOrUpdateLyrics(String songId, LyricResponse request) { // vừa request/response luôn
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        Lyrics lyrics = lyricsRepository.findBySongId(songId)
                .orElse(Lyrics.builder().song(song).build());

        lyrics.setInstrumental(request.isInstrumental());
        lyrics.setContent(request.getContent());

        Lyrics savedLyrics = lyricsRepository.save(lyrics);
        return lyricMapper.toLyricResponse(savedLyrics);
    }
}
