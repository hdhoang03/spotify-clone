package com.spotify.spotify.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String description;
    String name;
    String avatarUrl;
    //25/10
    String country;
    @Builder.Default //Không có thì set là 0 thay vì null
    Long followerCount = 0L;
    LocalDate debutDate;

    @Column(name = "is_deleted")
    @Builder.Default
    boolean deleted = false; //moi them

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<Song> songs = new HashSet<>();
}
