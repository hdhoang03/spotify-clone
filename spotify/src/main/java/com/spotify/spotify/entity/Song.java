package com.spotify.spotify.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String title;
    String artist;
    String album;
    @Column(nullable = true, length = 100)
    String genre;//Thể loại
    String coverUrl;
    String audioUrl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    User uploadedBy;
    LocalDateTime createAt;
}