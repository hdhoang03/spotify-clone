package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Lyrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LyricsRepository extends JpaRepository<Lyrics,String> {
    Optional<Lyrics> findBySongId(String songId);
}
