package com.booking.app.mapper;

import com.booking.app.dto.SearchHistoryDto;
import com.booking.app.entity.UserSearchHistory;
import com.booking.app.services.SearchHistoryService;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface HistoryMapper {

    @Mapping(source = "departureCityId", target = "departureCity")
    @Mapping(source = "arrivalCityId", target = "arrivalCity")
    @Mapping(source = "addingTime", target = "addingTime", qualifiedByName = "timeToString")
    SearchHistoryDto historyToDto(UserSearchHistory userSearchHistory);

    @Named("timeToString")
    static String timeToString(LocalDateTime time) {
    return time.format(DateTimeFormatter.ofPattern("hh:mm, d.M.yyyy"));


    }
}
