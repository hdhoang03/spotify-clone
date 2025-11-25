package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, String>, JpaSpecificationExecutor<Song> {
    Optional<Song> findByTitle(String songName);
    List<Song> findByArtist_Id(String artistId);
    List<Song> findByTitleContainingIgnoreCase(String keyword);
    List<Song> findByAlbum_Id(String albumId);
}
