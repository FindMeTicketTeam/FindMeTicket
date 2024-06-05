package com.booking.app.services.impl;

import com.booking.app.dto.CityDto;
import com.booking.app.mapper.UkrainianCitiesMapper;
import com.booking.app.services.LanguageDetectorService;
import com.booking.app.services.TypeAheadService;
import com.booking.app.services.UkrainianCitiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Service implementation for providing type-ahead suggestions for cities based on start letters.
 */
@Service
@RequiredArgsConstructor
public class TypeAheadServiceImpl implements TypeAheadService {

    private final LanguageDetectorService languageDetectorService;
    private final UkrainianCitiesService ukrainianCitiesService;

    private final UkrainianCitiesMapper ukrainianCitiesMapper;

    @Override
    public List<CityDto> findMatches(String startLetters, String siteLanguage) throws IOException {
        return languageDetectorService.detectLanguage(startLetters)
                .flatMap(language -> ukrainianCitiesService.getCities(startLetters, language)
                        .map(cities -> ukrainianCitiesMapper.toCitiesDtoList(cities, language, siteLanguage)))
                .orElseGet(Collections::emptyList);
    }

    @Override
    public Long getCityId(String city, String language) {
        return ukrainianCitiesService.getCities(city, language)
                .map(list -> list.isEmpty() ? null : list.getFirst().getId())
                .orElse(null);
    }

}
