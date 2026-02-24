package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, String> {

    interface AlbumWithSongCount{
        Album getAlbum();
        Long getSongCount();
    }

    @Query("""
            SELECT al AS album, COUNT(DISTINCT s.id) AS songCount
            FROM Album al
            LEFT JOIN al.songs s ON s.deleted = false
            LEFT JOIN al.artists ar
            WHERE al.deleted = :isDeleted
            AND (:keyword IS NULL OR LOWER(al.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                                     LOWER(ar.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            GROUP BY al.id
            """)
    Page<AlbumWithSongCount> searchAlbumsWithCount(@Param("keyword") String keyword,
                                                   @Param("isDeleted") boolean isDeleted,
                                                   Pageable pageable);

    boolean existsByNameAndArtists_Id(String name, String artistId);
    List<Album> findByArtists_Id(String artistId);
    @Query("""
            SELECT DISTINCT al FROM Album al
            LEFT JOIN al.artists ar
            WHERE al.deleted = false
            AND (
                LOWER(al.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR
                LOWER(ar.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
    """)
    List<Album> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}