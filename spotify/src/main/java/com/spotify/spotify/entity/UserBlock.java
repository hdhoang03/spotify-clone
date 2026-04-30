package com.spotify.spotify.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    User blocker;

    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    User blocked;

    @Column(name = "created_At")
    LocalDate createdAt;

    @PrePersist
    void onCreate(){
        createdAt = LocalDate.now();
    }
}
