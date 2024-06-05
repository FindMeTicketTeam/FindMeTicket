package com.booking.app.entity;

import com.booking.app.enums.TransportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SearchHistory {

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

    @ElementCollection(targetClass = TransportType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "type_transport")
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Set<TransportType> typeTransport = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
