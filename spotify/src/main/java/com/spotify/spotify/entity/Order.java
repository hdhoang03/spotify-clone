package com.spotify.spotify.entity;

import com.spotify.spotify.constaint.PlanType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders") // Đặt tên bảng là orders
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    Long orderCode;

    int amount;

    String description;

    String status; // "PENDING", "PAID", "CANCELLED"

    String userEmail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "plan_type")
    PlanType planType; //Monthly, Quarterly, Yearly

    LocalDateTime createdAt;
}
