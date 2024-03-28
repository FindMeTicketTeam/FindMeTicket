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
public class UserSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "departure_city")
    private Long departureCityId;

    @Column(name = "arrival_city")
    private Long arrivalCityId;

    @Column(name = "departure_date")
    private String departureDate;

    @Column(name = "adding_time")
    @Builder.Default
    private LocalDateTime addingTime = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
