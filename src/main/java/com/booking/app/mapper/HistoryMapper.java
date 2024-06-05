package com.booking.app.mapper;

import com.booking.app.dto.HistoryDto;
import com.booking.app.entity.SearchHistory;
import com.booking.app.enums.TransportType;
import com.booking.app.mapper.model.ArrivalCity;
import com.booking.app.mapper.model.DepartureCity;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, imports = {TransportType.class})
public interface HistoryMapper {

    @Mapping(source = "departureDate", target = "departureDate")
    @Mapping(source = "addingTime", target = "addingTime", qualifiedByName = "timeToString")
    @Mapping(source = "departureCityId", target = "departureCity", qualifiedByName = "getDepartureCity")
    @Mapping(source = "arrivalCityId", target = "arrivalCity", qualifiedByName = "getArrivalCity")
    @Mapping(target = "bus", expression = "java(searchHistory.getTypeTransport().contains(TransportType.BUS))")
    @Mapping(target = "train", expression = "java(searchHistory.getTypeTransport().contains(TransportType.TRAIN))")
    @Mapping(target = "airplane", expression = "java(searchHistory.getTypeTransport().contains(TransportType.AIRPLANE))")
    @Mapping(target = "ferry", expression = "java(searchHistory.getTypeTransport().contains(TransportType.FERRY))")
    HistoryDto historyToDto(SearchHistory searchHistory, @Context DepartureCity departureCity, @Context ArrivalCity arrivalCity);

    @Named("timeToString")
    static String timeToString(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("hh:mm a, dd.MM.yyyy"));
    }

    @Named("getDepartureCity")
    default String getDepartureCity(Long departureCityId, @Context DepartureCity departureCity) {
        return departureCity.name();
    }

    @Named("getArrivalCity")
    default String getArrivalCity(Long arrivalCityId, @Context ArrivalCity arrivalCity) {
        return arrivalCity.name();
    }

}
