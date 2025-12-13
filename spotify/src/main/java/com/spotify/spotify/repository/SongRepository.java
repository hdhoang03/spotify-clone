package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, String>, JpaSpecificationExecutor<Song> {
    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
    int incrementPlayCount(@Param("id") String id);

    Optional<Song> findByTitle(String songName);
    List<Song> findByArtist_Id(String artistId);
    List<Song> findByTitleContainingIgnoreCase(String keyword);
    Page<Song> findByAlbum_Id(String albumId, Pageable pageable);
}
