package com.booking.app.services.impl;

import com.booking.app.dto.RequestTicketsDTO;
import com.booking.app.dto.SearchHistoryDto;
import com.booking.app.entity.UkrainianPlaces;
import com.booking.app.entity.UserCredentials;
import com.booking.app.entity.UserSearchHistory;
import com.booking.app.enums.TypeTransportEnum;
import com.booking.app.mapper.HistoryMapper;
import com.booking.app.mapper.model.ArrivalCity;
import com.booking.app.mapper.model.DepartureCity;
import com.booking.app.repositories.SearchHistoryRepository;
import com.booking.app.repositories.UkrPlacesRepository;
import com.booking.app.repositories.UserCredentialsRepository;
import com.booking.app.services.SearchHistoryService;
import com.booking.app.services.TypeAheadService;
import com.booking.app.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.booking.app.constant.CustomHttpHeaders.USER_ID;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryRepository historyRepository;

    private final UserCredentialsRepository userCredentialsRepository;

    private final TypeAheadService typeAheadService;

    private final HistoryMapper historyMapper;

    private final UkrPlacesRepository ukrPlacesRepository;


    @Override
    public void addHistory(RequestTicketsDTO requestTicketsdto, String language, HttpServletRequest request) {
        Optional<UUID> uuid = CookieUtils.getCookie(request, USER_ID).map(cookie -> UUID.fromString(cookie.getValue()));

        Optional<UserCredentials> userCredentials = uuid.flatMap(userCredentialsRepository::findById);

        userCredentials.ifPresent(user -> {
            Set<TypeTransportEnum> types = TypeTransportEnum.getTypes(requestTicketsdto.getBus(), requestTicketsdto.getTrain(), requestTicketsdto.getAirplane(), requestTicketsdto.getFerry());
            historyRepository.save(UserSearchHistory.builder()
                    .user(user.getUser())
                    .departureCityId(typeAheadService.getCityId(requestTicketsdto.getDepartureCity(), language))
                    .arrivalCityId(typeAheadService.getCityId(requestTicketsdto.getArrivalCity(), language))
                    .departureDate(requestTicketsdto.getDepartureDate())
                    .typeTransport(types)
                    .build());
        });
    }

    @Override
    public List<SearchHistoryDto> getUserHistory(UserCredentials userCredentials, String language) {
        return Stream.of(userCredentials)
                .flatMap(user -> user.getUser().getHistory().stream())
                .map(history -> {
                    String departureCity = getCity(history.getArrivalCityId(), language);
                    String arrivalCity = getCity(history.getDepartureCityId(), language);
                    return historyMapper.historyToDto(history, new DepartureCity(arrivalCity), new ArrivalCity(departureCity));
                })
                .toList().reversed();
    }

    private String getCity(Long id, String language) {
        Optional<String> city = language.equals("eng") ? ukrPlacesRepository.findById(id).map(UkrainianPlaces::getNameEng)
                : ukrPlacesRepository.findById(id).map(UkrainianPlaces::getNameUa);
        return city.orElse(null);
    }

}
