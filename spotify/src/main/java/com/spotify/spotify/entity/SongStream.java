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
@NoArgsConstructor
@AllArgsConstructor
public class SongStream {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id; //Mỗi lượt nghe là 1 record riêng để chạy analytics

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user; //Người nghe bài hát, nếu chưa đăng nhập sẽ không tính lượt stream

    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    Song song; //Bài hát được nghe

    Long duration; //Thời gian nghe, ví dụ 36s trở lên là 1 lần

    @Column(nullable = false)
    LocalDateTime createdAt;
}