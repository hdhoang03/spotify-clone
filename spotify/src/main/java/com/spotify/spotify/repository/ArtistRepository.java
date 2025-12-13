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
    Optional<Artist> findByIdAndDeletedFalse(String artist);
    Page<Artist> findAllByDeletedFalse(Pageable pageable); //nghệ sĩ chưa xóa
    boolean existsByNameIgnoreCase(String name);
    Page<Artist> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

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