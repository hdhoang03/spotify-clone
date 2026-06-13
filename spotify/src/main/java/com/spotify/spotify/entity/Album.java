package com.spotify.spotify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Album {
    @Id
    /*
    * Primary Key strategy: UUID v4. Optimized for security;
    * awareness of index fragmentation exists,
    * planned to migrate to UUID v7/Snowflake if scaling is required.
    * */
//    @UuidGenerator(style = UuidGenerator.Style.TIME)
//    @Column(columnDefinition = "BINARY(16)")
//    UUID id;
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String description;
    String albumUrl;
    @ManyToMany
    @JoinTable(name = "album_artist", joinColumns = @JoinColumn(name = "album_id"), inverseJoinColumns = @JoinColumn(name = "artist_id"))
    Set<Artist> artists = new HashSet<>();
    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    @JsonIgnore
    Set<Song> songs = new HashSet<>();

    LocalDate releaseDate; //25/10

    @Column(name = "is_deleted")
    @Builder.Default
    boolean deleted = false;
}