package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, String> {

    interface ArtistWithSongCount { //Hứng dữ liệu gộp
        Artist getArtist();
        Long getSongCount();
    }

    @Query("""
            SELECT a AS artist, COUNT(s.id) AS songCount
            FROM Artist a
            LEFT JOIN a.songs s ON s.deleted = false
            WHERE a.deleted = :deleted
            GROUP BY a.id
            """)
    Page<ArtistWithSongCount> findAllWithSongCount(@Param("deleted") boolean deleted, Pageable pageable);

    @Query("""
            SELECT a AS artist, COUNT(s.id) AS songCount
            FROM Artist a
            LEFT JOIN a.songs s ON s.deleted = false
            WHERE a.deleted = :deleted AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            GROUP BY a.id
            """)
    Page<ArtistWithSongCount> searchWithSongCount(@Param("keyword") String keyword, @Param("deleted") boolean deleted, Pageable pageable);

    Optional<Artist> findByIdAndDeletedFalse(String artist);
    Page<Artist> findAllByDeleted(boolean deleted, Pageable pageable); //nghệ sĩ chưa xóa
    boolean existsByNameIgnoreCase(String name);
    Page<Artist> findByNameContainingIgnoreCaseAndDeleted(String keyword, boolean deleted, Pageable pageable);

    //COALESCE(a.followerCount, 0) giúp tránh lỗi nếu ban đầu count đang bị NULL
    @Modifying
    @Query("""
            UPDATE Artist a
            SET a.followerCount = COALESCE(a.followerCount, 0) + 1
            WHERE a.id = :id
            """)
    void incrementFollowerCount(@Param("id") String id);

    @Modifying
    @Query("""
            UPDATE Artist a
            SET a.followerCount = COALESCE(a.followerCount, 0) - 1
            WHERE a.id = :id AND a.followerCount > 0
            """)
    void decrementFollowerCount(@Param("id") String id);
}