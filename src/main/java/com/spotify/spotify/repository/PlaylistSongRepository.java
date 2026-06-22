package com.spotify.spotify.repository;

import com.spotify.spotify.entity.PlaylistSong;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, String> {
    @Query("SELECT ps FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId AND ps.song.deleted = false")
    Page<PlaylistSong> findByPlaylistIdOrderByAddedAtDesc(String playlistId, Pageable pageable);

//    Hoặc cái này cũng được
//    Page<PlaylistSong> findByPlaylist_IdAndSong_DeletedFalseOrderByAddedAtDesc(String playlistId, Pageable pageable);
    Optional<PlaylistSong> findByPlaylistIdAndSongId(String playlistId, String songId);

    boolean existsByPlaylistIdAndSongId(String playlistId, String songId);

    @Modifying
    @Query("DELETE FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId AND ps.song.id = :songId")
    void deleteByPlaylistIdAndSongId(@Param("playlistId") String playlistId, @Param("songId") String songId);
}
