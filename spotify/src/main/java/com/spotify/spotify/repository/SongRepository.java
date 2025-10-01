package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, String> {
    Optional<Song> findByTitle(String songName);
    Optional<Song> findByArtist(String artist);
    List<Song> findByTitleContaining(String keyword);
}
