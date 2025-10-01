package com.spotify.spotify.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
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
    String password;
    String name;//fname + lname
    @Column(name = "email", unique = true)
    String email;
    LocalDate dob;
    Boolean enabled;//Chặn tài khoản
    @ManyToMany
    Set<Role> roles;
    @ManyToMany
    Set<Permission> permissions;
//    @OneToMany(mappedBy = "uploadedBy") //Danh sách bài hát mà user đã tải lên
//    Set<Song> songs;
}
