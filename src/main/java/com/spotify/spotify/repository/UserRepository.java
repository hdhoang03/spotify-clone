package com.spotify.spotify.repository;

import com.spotify.spotify.dto.response.UserGrowthResponse;
import com.spotify.spotify.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    @Query("""
        SELECT u FROM User u WHERE
        (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND NOT EXISTS (
            SELECT ub FROM UserBlock ub
            WHERE (ub.blocker.id = :currentUserId AND ub.blocked.id = u.id)
               OR (ub.blocker.id = u.id AND ub.blocked.id = :currentUserId)
        )
    """)
    Page<User> searchUsersMultiColumns(@Param("keyword") String keyword,
                                       @Param("currentUserId") String currentUserId,
                                       Pageable pageable);

    @Query("""
        SELECT new com.spotify.spotify.dto.response.UserGrowthResponse(
            CONCAT('Tháng ', MONTH(u.createdAt)), COUNT(u.id)
        )
        FROM User u
        WHERE YEAR(u.createdAt) = :year
        GROUP BY MONTH(u.createdAt), CONCAT('Tháng ', MONTH(u.createdAt))
        ORDER BY MONTH(u.createdAt) ASC
        """)
    List<UserGrowthResponse> countUserGrowthByYear(@Param("year") int year);

    @Query("""
        SELECT u FROM User u WHERE
        (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
         AND u.enabled = true
         AND NOT EXISTS (
             SELECT ub FROM UserBlock ub
             WHERE (ub.blocker.id = :currentUserId AND ub.blocked.id = u.id)
                OR (ub.blocker.id = u.id AND ub.blocked.id = :currentUserId)
             )
    """)
    Page<User> searchUsersForGlobalSearch(@Param("keyword") String keyword,
                                          @Param("currentUserId") String currentUserId,
                                          Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isPremium = false, u.premiumExpiryDate = null " +
            "WHERE u.isPremium = true AND u.premiumExpiryDate < :now")
    int deactiveExpiredPremiumUsers(@Param("now") LocalDateTime now);
}