package com.spotify.spotify.entity;

import com.spotify.spotify.constaint.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(nullable = false, unique = true, length = 100)
    String name;
    String description;
    @Enumerated(EnumType.STRING)
    CategoryType type;
    String coverUrl;
    Boolean active;
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false)
    Set<Song> songs = new HashSet<>();
}