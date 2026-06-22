package com.spotify.spotify.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "song_streams", indexes = {
        @Index(name = "idx_user_time", columnList = "user_id, createdAt"),
        @Index(name = "idx_song_valid", columnList = "song_id, is_valid_stream")
})
@NoArgsConstructor
@AllArgsConstructor
public class SongStream {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id; //Mỗi lượt nghe là 1 record riêng để chạy analytics

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user; //Người nghe bài hát, nếu chưa đăng nhập sẽ không tính lượt stream

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    Song song; //Bài hát được nghe

    Long duration; //Thời gian nghe, ví dụ 36s trở lên là 1 lần

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(name = "is_valid_stream", nullable = false)
    @Builder.Default
    boolean validStream = false;

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
        if (this.duration != null && this.duration >= 30){
           this.validStream = true;
        }
    }
}