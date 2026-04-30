package com.spotify.spotify.repository;

import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.entity.Playlist;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    Page<Playlist> findByUserId(String userId, Pageable pageable);
    Page<Playlist> findByUserIdAndIsPublicTrue(String userId, Pageable pageable);

    // Sử dụng EntityGraph để FETCH gộp các bảng liên quan trong 1 câu Query duy nhất
    @EntityGraph(attributePaths = {"songs", "user"})
    Page<Playlist> findByUser_Id(String targetUserId, Pageable pageable);
    Page<Playlist> findByUser_IdAndIsPublicTrue(String targetUserId, Pageable pageable);

    @EntityGraph(attributePaths = {"playlistSongs", "user"})
    Optional<Playlist> findById(String id);

    long countByUser_Id(String userId);
    long countByUser_IdAndIsPublicTrue(String userId);

    @Query("SELECT new com.spotify.spotify.dto.response.PlaylistResponse(" +
            "p.id, p.name, p.description, p.coverUrl, p.isPublic, p.createdAt, " +
            "new com.spotify.spotify.dto.response.UserSummaryResponse(u.id, u.username, u.avatarUrl), " +
            "CAST(COUNT(ps.id) AS int)) " +
            "FROM Playlist p " +
            "LEFT JOIN p.user u " +
            "LEFT JOIN p.playlistSongs ps " +
            "WHERE u.id = :userId " +
            "GROUP BY p.id, p.name, p.description, p.coverUrl, p.isPublic, p.createdAt, u.id, u.username, u.avatarUrl")
    Page<PlaylistResponse> findMyPlaylistsWithCount(@Param("userId") String userId, Pageable pageable);

    // lấy danh sách Playlist Công Khai của User khác
    @Query("SELECT new com.spotify.spotify.dto.response.PlaylistResponse(" +
            "p.id, p.name, p.description, p.coverUrl, p.isPublic, p.createdAt, " +
            "new com.spotify.spotify.dto.response.UserSummaryResponse(u.id, u.username, u.avatarUrl), " +
            "CAST(COUNT(ps.id) AS int)) " +
            "FROM Playlist p " +
            "LEFT JOIN p.user u " +
            "LEFT JOIN p.playlistSongs ps " +
            "WHERE u.id = :userId AND p.isPublic = true " +
            "GROUP BY p.id, p.name, p.description, p.coverUrl, p.isPublic, p.createdAt, u.id, u.username, u.avatarUrl")
    Page<PlaylistResponse> findPublicPlaylistsWithCount(@Param("userId") String userId, Pageable pageable);
}
