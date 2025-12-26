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
    @Query("UPDATE Song s SET s.playCount = s.playCount + :amount WHERE s.id = :id")
    void incrementPlayCountAmount(@Param("id") String id, @Param("amount") Long amount); //Cộng n lần cùng lúc

    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
    int incrementPlayCount(@Param("id") String id); //Cộng 1

    List<Song> findByArtist_Id(String artistId);
    Page<Song> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);
    Page<Song> findByAlbum_Id(String albumId, Pageable pageable);
    @Query("""
            SELECT s FROM Song s
            LEFT JOIN FETCH s.album
            LEFT JOIN FETCH s.artist a
            LEFT JOIN FETCH s.category
            WHERE s.deleted = false
            AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR
                (a.id IS NOT NULL AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            )
    """)
    List<Song> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Song s
            SET s.likeCount = COALESCE(s.likeCount, 0) + 1
            WHERE s.id =:id
            """)
    void incrementLikeCount(@Param("id") String id);

    @Modifying
    @Query("""
            UPDATE Song s
            SET s.likeCount = COALESCE(s.likeCount, 0) - 1
            WHERE s.id =:id AND s.likeCount > 0
            """)
    void decrementLikeCount(@Param("id") String id);
}
