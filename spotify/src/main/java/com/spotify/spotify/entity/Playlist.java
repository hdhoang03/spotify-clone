package com.spotify.spotify.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Playlist {// Cho user tạo playlist thêm/xóa bài, chia sẻ bài nhạc
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String description;
    String coverUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    Boolean isPublic = true;

    @ManyToMany
    @JoinTable(name = "playlist_song",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    Set<Song> songs = new HashSet<>();

    @Column(name = "create_at")//sai tên cột nên phải định nghĩa lại mới mapping được
    LocalDateTime createdAt;
    @PrePersist//Cập nhật thời gian tự động
    void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
