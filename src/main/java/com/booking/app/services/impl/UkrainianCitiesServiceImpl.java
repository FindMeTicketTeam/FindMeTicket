package com.booking.app.services.impl;

import com.booking.app.entity.UkrainianCities;
import com.booking.app.repositories.UkrainianCitiesRepository;
import com.booking.app.services.UkrainianCitiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UkrainianCitiesServiceImpl implements UkrainianCitiesService {

    private final UkrainianCitiesRepository ukrainianCitiesRepository;

    @Override
    public String getCity(Long id, String language) {
        Optional<String> city = language.equals("eng")
                ? ukrainianCitiesRepository.findById(id).map(UkrainianCities::getNameEng)
                : ukrainianCitiesRepository.findById(id).map(UkrainianCities::getNameUa);
        return city.orElse(null);
    }

    public Optional<List<UkrainianCities>> getCities(String startLetters, String inputLanguage) {
        return (inputLanguage.equals("eng"))
                ? ukrainianCitiesRepository.findUkrainianPlacesByNameEngStartsWithIgnoreCaseAndNameEngNotContainingIgnoreCase(startLetters, "oblast")
                : ukrainianCitiesRepository.findUkrainianPlacesByNameUaStartsWithIgnoreCaseAndNameUaNotContainingIgnoreCase(startLetters, "область");
    }

}
