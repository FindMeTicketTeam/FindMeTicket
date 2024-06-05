package com.booking.app.services;


import com.booking.app.entity.UkrainianCities;

import java.util.List;
import java.util.Optional;

public interface UkrainianCitiesService {

    /**
     * Retrieves the name of a city based on its ID and language.
     *
     * @param id       The ID of the city.
     * @param language The language for the city name.
     * @return The name of the city.
     */
    String getCity(Long id, String language);

    /**
     * Retrieves a list of UkrainianPlaces based on start letters and language.
     *
     * @param startLetters  The start letters of the city name.
     * @param inputLanguage The language of the city name.
     * @return Optional list of UkrainianPlaces.
     */
    Optional<List<UkrainianCities>> getCities(String startLetters, String inputLanguage);

}
