package com.spotify.spotify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.spotify.spotify.constaint.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true, length = 100)
    String name;
    String description;
    String backgroundColor; //Lưu mã màu HEX

    @Enumerated(EnumType.STRING)
    CategoryType type;
    String coverUrl;

    @Builder.Default
    boolean active = true;

//    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false)
//    @JsonManagedReference
//    Set<Song> songs = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "song_category",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    @JsonIgnore
    Set<Song> songs = new HashSet<>();

    Integer displayOrder; //Thứ tự hiển thị 25/10

    @Column(name = "is_deleted")
    @Builder.Default
    boolean deleted = false;
}