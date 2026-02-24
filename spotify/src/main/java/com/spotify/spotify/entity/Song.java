package com.spotify.spotify.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "songs")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String title;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    Artist artist;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    @JsonBackReference
    Album album;
    String coverUrl;
    String audioUrl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    User uploadedBy;
//    @ManyToMany(mappedBy = "songs") //một bài hát thuộc nhiều thể loại
//    Set<Category> categories = new HashSet<>();
    @JsonBackReference
    @ManyToOne//Một bài hát thuộc một thể loại
    @JoinColumn(name = "category_id")
    Category category;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    //để playcount và likecount để mỗi khi chạy không cần tốn tài nguyên query lại
    @Builder.Default//Để không gán null mà gán là 0
    Long playCount = 0L;
    @Builder.Default
    Long likeCount = 0L;
    Double duration;

    @Column(name = "is_deleted")
    @Builder.Default
    boolean deleted = false;

    LocalDate releaseDate;
    
    @OneToMany(mappedBy = "song", cascade = CascadeType.REMOVE)
    Set<LikeSong> likes = new HashSet<>();

    @OneToMany(mappedBy = "song", cascade = CascadeType.REMOVE)//khi xóa bài hát tự động xóa luôn like và stream
    Set<SongStream> streams = new HashSet<>();

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    //Thêm mới: Danh sách nghệ sĩ kết hợp ngày 6/1/2026
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "song_performers",
        joinColumns = @JoinColumn(name = "song_id"),
        inverseJoinColumns = @JoinColumn(name = "artist_id"")
    )
    @Builder.Default
    Set<Artist> featuredArtists = new HashSet<>();

    //Để remove song hoạt động
//    @Override
//    public boolean equals(Object o){
//        if (this ==  o) return true;
//        if (!(o instanceof Song)) return false;
//        Song song = (Song) o;
//        return id != null && id.equals(song.id);
//    }
//
//    @Override
//    public int hashCode(){
//        return getClass().hashCode();
//    }
}
