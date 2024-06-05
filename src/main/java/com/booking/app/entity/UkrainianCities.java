package com.booking.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UkrainianCities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_eng")
    private String nameEng;

    private String country;

    private Double lon;

    private Double lat;

    @Column(name = "name_ua")
    private String nameUa;

    @Column(name = "name_ru")
    private String nameRu;
}
