package com.spotify.spotify.entity;

import com.spotify.spotify.configuration.LyricsConverter;
import com.spotify.spotify.dto.response.LyricLine;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Lyrics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    Song song;

    @Convert(converter = LyricsConverter.class)
    @Column(columnDefinition = "JSON")
    List<LyricLine> content;

    boolean isInstrumental;
}
