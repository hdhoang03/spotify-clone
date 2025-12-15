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
    boolean existsByNameAndArtists_Id(String name, String artistId);
    List<Album> findByNameContaining(String keyword);
    List<Album> findByArtists_Id(String artistId);
    Page<Album> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);
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