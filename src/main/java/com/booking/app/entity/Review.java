package com.booking.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "varchar(1000)")
    private String reviewText;

    @Builder.Default
    private LocalDateTime addingDate = LocalDateTime.now();

    private int grade;

    @OneToOne
    @JoinColumn(referencedColumnName = "id", name = "user_id")
    private User user;

}
