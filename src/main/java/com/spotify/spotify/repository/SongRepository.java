package com.spotify.spotify.repository;

import com.spotify.spotify.dto.response.GenreStatResponse;
import com.spotify.spotify.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
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
    @Query("UPDATE Song s SET s.playCount = COALESCE(s.playCount, 0) + 1 WHERE s.id = :id")
    void incrementPlayCount(@Param("id") String id); //Cộng 1

    //COALESCE để chống lỗi NULL
    @Modifying
    @Query("UPDATE Song s SET s.streamCount = COALESCE(s.streamCount, 0) + 1 WHERE s.id = :id")
    void incrementStreamCount(@Param("id") String id);

    // Dùng EntityGraph để fetch các quan hệ ManyToOne ngay lập tứ
    @EntityGraph(attributePaths = {"artist", "album", "category"})
    @Query("SELECT s FROM Song s WHERE s.deleted = false AND s.artist.deleted = false AND LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Song> searchActiveSongByTitle(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"artist", "album", "category"})
    Page<Song> findByAlbum_Id(String albumId, Pageable pageable);

    @EntityGraph(attributePaths = {"artist", "album", "category"})
    Page<Song> findByCategory_Id(String categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"artist", "album", "category"})
    @Query("SELECT s FROM Song s WHERE s.deleted = false AND s.artist.deleted = false")
    Page<Song> findAllActiveSongs(Pageable pageable);

    //Pha 1 Chỉ đi tìm ID của 5 bài hát khớp với từ khóa
    @Query("""
        SELECT s.id FROM Song s
        LEFT JOIN s.artist a
        WHERE s.deleted = false
        AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR (a.id IS NOT NULL AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))))
    """)
    List<String> findSongIdsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    //Pha 2 Từ 5 cái ID ở trên, ta kéo FETCH toàn bộ dữ liệu con lên
    @Query("""
        SELECT DISTINCT s FROM Song s
        LEFT JOIN FETCH s.album
        LEFT JOIN FETCH s.artist
        LEFT JOIN FETCH s.category
        LEFT JOIN FETCH s.featuredArtists
        WHERE s.id IN :ids
    """)
    List<Song> findSongsWithDetailsByIds(@Param("ids") List<String> ids);

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

    @Query("""
        SELECT new com.spotify.spotify.dto.response.GenreStatResponse(
                c.name, COUNT(s.id), ''
                )
        FROM Song s
        JOIN s.category c
        WHERE s.deleted = false
        GROUP BY c.id, c.name
        ORDER BY COUNT(s.id) DESC
        """)
    List<GenreStatResponse> countSongsByCategory();

    @Query("""
        SELECT DISTINCT s FROM Song s
        LEFT JOIN s.featuredArtists fa
        WHERE (s.artist.id = :artistId OR fa.id = :artistId) AND s.deleted = false
        ORDER BY s.streamCount DESC
    """)
    List<Song> findTopPopularSongsByArtist(@Param("artistId") String artistId);

    long countByAlbum_IdAndDeletedFalse(String albumId);

    long countByDeletedFalse();
}
