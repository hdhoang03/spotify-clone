package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    Page<Playlist> findByUserId(String userId, Pageable pageable);
    Page<Playlist> findByUserIdAndIsPublicTrue(String userId, Pageable pageable);
}
