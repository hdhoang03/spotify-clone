package com.spotify.spotify.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "username", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;

    @Column(nullable = false)
    String password;

    String name;

    @Column(name = "email", unique = true, nullable = false)
    String email;

    LocalDate dob;

    Boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<Role> roles;

    @ManyToMany
    Set<Permission> permissions;

    String avatarUrl;
    String bio;

    @Column(name = "created_at")
    LocalDate createAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Playlist> playlists = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    Set<LikeSong> likes = new HashSet<>();

    @Builder.Default
    boolean isPublicProfile = true;

    @PrePersist
    void onCreate(){
        createAt = LocalDate.now();
    }
}
