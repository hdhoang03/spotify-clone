package com.spotify.spotify.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    Album album;
//    @Column(nullable = true, length = 100)
//    String genre;//Thể loại
    String coverUrl;
    String audioUrl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    User uploadedBy;
//    @ManyToMany(mappedBy = "songs") //một bài hát thuộc nhiều thể loại
//    Set<Category> categories = new HashSet<>();
    @ManyToOne//Một bài hát thuộc một thể loại
    @JoinColumn(name = "category_id")
    Category category;
    LocalDateTime createdAt;
}