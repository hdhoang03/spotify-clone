package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    //Lấy thông báo mới nhất của người dùng cụ thể
    Page<Notification> findByRecipient_UsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    //Số lượng tin nhắn chưa đọc
    long countByRecipient_UsernameAndIsReadFalse(String username);

    @Modifying
    @Query("""
        UPDATE Notification n SET n.isRead = TRUE
        WHERE n.recipient.username = :username
        AND n.isRead = false
    """)
    void markAllAsRead(@Param("username") String username);

    //Xóa thông báo cũ hơn ngày chỉ định tránh rác database
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void cleanOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    Optional<Notification> findByIdAndRecipient_Username(String id, String username);
}