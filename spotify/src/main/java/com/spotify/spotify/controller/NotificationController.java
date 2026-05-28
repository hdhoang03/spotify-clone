package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.request.AdminNotificationRequest;
import com.spotify.spotify.dto.response.NotificationResponse;
import com.spotify.spotify.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/notification")
public class NotificationController {
    NotificationService notificationService;

    @PostMapping("/admin/broadcast")
    ApiResponse<Void> pushSystemNotification(@RequestBody AdminNotificationRequest request) {
        notificationService.broadcastSystemNotification(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Admin Notification Pushed")
                .build();
    }

    @GetMapping("/my")
    ApiResponse<Page<NotificationResponse>> getMyNotification(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<Page<NotificationResponse>>builder()
                .code(1000)
                .result(notificationService.getMyNotifications(PageRequest.of(page - 1, size)))
                .build();
    }

    @GetMapping("/unread-count")
    ApiResponse<Long> getUnreadCount(){
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Unread Count")
                .result(notificationService.getUnreadCount())
                .build();
    }

    @PutMapping("/{id}/read")
    ApiResponse<Boolean> toggleReadStatus(@PathVariable String id){
        boolean newStatus = notificationService.toggleReadStatus(id);
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .message(newStatus ? "Marked as read" : "Marked as unread")
                .result(newStatus)
                .build();
    }

    @PutMapping("/read-all")
    ApiResponse<Void> markAllAsRead(){
        notificationService.markAllAsRead();
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Marked all as read")
                .build();
    }

    @DeleteMapping("/{id}/delete")
    ApiResponse<Void> deleteNotification(@PathVariable String id){
        notificationService.deleteNotification(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Notification deleted successfully.")
                .build();
    }
}
