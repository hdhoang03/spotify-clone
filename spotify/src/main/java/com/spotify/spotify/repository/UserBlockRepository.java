package com.spotify.spotify.repository;

import com.spotify.spotify.entity.UserBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, String> {
    Optional<UserBlock> findByBlocker_IdAndBlocked_Id(String blockerId, String blockedId);

    @Query("SELECT COUNT(ub) > 0 FROM UserBlock ub WHERE " +
    "(ub.blocker.id = :user1 AND ub.blocked.id = :user2) OR " +
    "(ub.blocker.id = :user2 AND ub.blocked.id = :user1)")
    boolean existsBlockBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    Page<UserBlock> findByBlocker_Id(String blockedId, Pageable pageable);
}
