package com.booking.app.repositories;

import com.booking.app.entity.UkrainianCities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UkrainianCitiesRepository extends JpaRepository<UkrainianCities, Long> {

    Optional<List<UkrainianCities>> findUkrainianPlacesByNameEngStartsWithIgnoreCaseAndNameEngNotContainingIgnoreCase(String startLetters, String exclusion);

    Optional<List<UkrainianCities>> findUkrainianPlacesByNameUaStartsWithIgnoreCaseAndNameUaNotContainingIgnoreCase(String startLetters, String exclusion);

}
