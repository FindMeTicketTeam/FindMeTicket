package com.booking.app.services.impl;

import com.booking.app.dto.CitiesDTO;
import com.booking.app.dto.StartLettersDTO;
import com.booking.app.entity.UkrainianPlaces;
import com.booking.app.mapper.UkrainianPlacesMapper;
import com.booking.app.repositories.UkrPlacesRepository;
import com.booking.app.services.LanguageDetectorService;
import com.booking.app.services.TypeAheadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for providing type-ahead suggestions for cities based on start letters.
 */
@Service
@RequiredArgsConstructor
public class TypeAheadServiceImpl implements TypeAheadService {

    private final UkrPlacesRepository ukrPlacesRepository;

    private final LanguageDetectorService languageDetectorService;

    private final UkrainianPlacesMapper ukrainianPlacesMapper;

    /**
     * Find matching cities in Ukraine based on start letters and site language.
     *
     * @param startLetters DTO containing the start letters for matching cities.
     * @param siteLanguage Language of the site.
     * @return List of CitiesDTO representing matching cities.
     * @throws IOException If an I/O error occurs during the process.
     */
    @Override
    public List<CitiesDTO> findMatches(StartLettersDTO startLetters, String siteLanguage) throws IOException {
        String inputLanguage = languageDetectorService.detectLanguage(startLetters.getStartLetters()).orElse(null);

        if (inputLanguage == null) return Collections.emptyList();

        Optional<List<UkrainianPlaces>> listOfPlaces = getListOfPlaces(startLetters.getStartLetters(), inputLanguage);

        return listOfPlaces.map(places -> ukrainianPlacesMapper.toCitiesDTOList(places, inputLanguage, siteLanguage))
                .orElse(Collections.emptyList());
    }

    @Override
    public Long getCityId(String City, String language){
        return getListOfPlaces(City, language).orElse(null).get(0).getId();
    }

    /**
     * Get the list of places based on start letters and input language.
     *
     * @param startLetters  DTO containing the start letters for matching places.
     * @param inputLanguage Detected input language.
     * @return Optional list of UkrainianPlaces matching the criteria.
     */

    private Optional<List<UkrainianPlaces>> getListOfPlaces(String startLetters, String inputLanguage) {
        return (inputLanguage.equals("eng"))
                ? ukrPlacesRepository.findUkrainianPlacesByNameEngStartsWithIgnoreCaseAndNameEngNotContainingIgnoreCase(startLetters, "oblast")
                : ukrPlacesRepository.findUkrainianPlacesByNameUaStartsWithIgnoreCaseAndNameUaNotContainingIgnoreCase(startLetters, "область");
    }


}
