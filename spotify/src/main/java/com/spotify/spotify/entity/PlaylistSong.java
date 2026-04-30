package com.spotify.spotify.entity;

import jakarta.persistence.*;
import jdk.jfr.Name;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "playlist_song")
public class PlaylistSong {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    Song song;

    @Column(name = "added_at")
    LocalDate addedAt;

    @PrePersist
    void onCreate(){
        addedAt = LocalDate.now();
    }
}
