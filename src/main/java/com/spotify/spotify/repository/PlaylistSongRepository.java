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
    // JOIN FETCH để load song + artist + album trong 1 SQL query, tránh N+1
    // ManyToOne FETCH JOIN an toàn với Pageable (không bị in-memory pagination warning)
    @Query("SELECT ps FROM PlaylistSong ps " +
            "JOIN FETCH ps.song s " +
            "LEFT JOIN FETCH s.artist a " +
            "LEFT JOIN FETCH s.album alb " +
            "WHERE ps.playlist.id = :playlistId " +
            "ORDER BY ps.addedAt DESC")
    Page<PlaylistSong> findByPlaylistIdOrderByAddedAtDesc(@Param("playlistId") String playlistId, Pageable pageable);

//    Hoặc cái này cũng được
//    Page<PlaylistSong> findByPlaylist_IdAndSong_DeletedFalseOrderByAddedAtDesc(String playlistId, Pageable pageable);
    Optional<PlaylistSong> findByPlaylistIdAndSongId(String playlistId, String songId);

    boolean existsByPlaylistIdAndSongId(String playlistId, String songId);

    @Modifying
    @Query("DELETE FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId AND ps.song.id = :songId")
    void deleteByPlaylistIdAndSongId(@Param("playlistId") String playlistId, @Param("songId") String songId);

    // Tìm kiếm bài hát trong playlist theo keyword (title hoặc tên artist)
    // JOIN FETCH để filter + load trong 1 query, JOIN thường chỉ để filter sẽ gây N+1
    @Query("SELECT ps FROM PlaylistSong ps " +
            "JOIN FETCH ps.song s " +
            "LEFT JOIN FETCH s.artist a " +
            "LEFT JOIN FETCH s.album alb " +
            "WHERE ps.playlist.id = :playlistId " +
            "AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "  OR LOWER(a.name)  LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY ps.addedAt DESC")
    Page<PlaylistSong> searchInPlaylist(@Param("playlistId") String playlistId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);
}
