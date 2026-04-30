package com.spotify.spotify.repository;

import com.spotify.spotify.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, String> {
    Optional<UserFollow> findByFollower_IdAndFollowing_Id(String followerId, String followingId);
    boolean existsByFollower_IdAndFollowing_Id(String followerId, String followingId);
    Page<UserFollow> findByFollower_Id(String followerId, Pageable pageable);
    Page<UserFollow> findByFollowing_Id(String followerId, Pageable pageable);
}
