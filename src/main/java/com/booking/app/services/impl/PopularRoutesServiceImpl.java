package com.booking.app.services.impl;


import com.booking.app.constant.PopularRoutesConstants;
import com.booking.app.dto.City;
import com.booking.app.dto.RequestTicketsDto;
import com.booking.app.entity.UkrainianCities;
import com.booking.app.repositories.UkrainianCitiesRepository;
import com.booking.app.services.PopularRoutesService;
import com.booking.app.services.impl.scrape.ScraperManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.booking.app.constant.DateFormatConstants.DATE_FORMAT_PATTERN;

/**
 * Service implementation for finding popular routes.
 */
@Service
@RequiredArgsConstructor
public class PopularRoutesServiceImpl implements PopularRoutesService {

    public static final String UKRAINIAN = "ua";
    public static final String ENGLISH = "eng";

    private final ScraperManager manager;

    private final UkrainianCitiesRepository placesRepository;

    @Override
    @Async
    public void findRoutes() throws IOException, ParseException {
        List<City> routes = PopularRoutesConstants.getPopularRoutes();
        List<CompletableFuture<Boolean>> runningRoutes = new LinkedList<>();

        for (City route : routes) {
            Optional<UkrainianCities> departureCity = placesRepository.findById(route.departureId());
            Optional<UkrainianCities> arrivalCity = placesRepository.findById(route.arrivalId());

            if (departureCity.isPresent() && arrivalCity.isPresent()) {
                runningRoutes.add(findTickets(departureCity.get().getNameUa(), arrivalCity.get().getNameUa(), UKRAINIAN));
                runningRoutes.add(findTickets(departureCity.get().getNameEng(), arrivalCity.get().getNameEng(), ENGLISH));
                runningRoutes.add(findTickets(arrivalCity.get().getNameUa(), departureCity.get().getNameUa(), UKRAINIAN));
                runningRoutes.add(findTickets(arrivalCity.get().getNameEng(), departureCity.get().getNameEng(), ENGLISH));

                CompletableFuture.allOf(runningRoutes.toArray((CompletableFuture[]::new))).join();
                runningRoutes.clear();
            }

        }
    }

    /**
     * Initiates a search for tickets.
     *
     * @param departureCity the name of the departure city
     * @param arrivalCity   the name of the arrival city
     * @param language      the language code
     * @return a CompletableFuture indicating the success of the ticket search
     * @throws IOException    if an I/O error occurs during the ticket search
     * @throws ParseException if there is an error in parsing the date
     */
    private CompletableFuture<Boolean> findTickets(String departureCity, String arrivalCity, String language) throws IOException, ParseException {
        return manager.findTickets(RequestTicketsDto.builder()
                .departureCity(departureCity)
                .arrivalCity(arrivalCity)
                .departureDate(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)))
                .build(), null, language);
    }

}
