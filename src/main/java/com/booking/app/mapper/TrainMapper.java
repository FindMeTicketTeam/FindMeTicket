package com.booking.app.mapper;

import com.booking.app.dto.TicketDto;
import com.booking.app.dto.TrainComfortInfoDTO;
import com.booking.app.entity.ticket.train.TrainInfo;
import com.booking.app.entity.ticket.train.TrainTicket;
import com.booking.app.exception.exception.UndefinedLanguageException;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true))
public interface TrainMapper {

    @Mapping(source = "route.departureCity", target = "departureCity")
    @Mapping(source = "route.arrivalCity", target = "arrivalCity")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "route.departureDate", target = "departureDate", qualifiedByName = "departureTimeMapping")
    @Mapping(source = "travelTime", target = "travelTime", qualifiedByName = "decimalToString")
    @Mapping(source = "carrier", target = "carrier")
    @Mapping(target = "type", constant = "TRAIN")
    TicketDto toTrainTicketDto(TrainTicket ticket, @Context String language);

    @Mapping(source = "link", target = "url")
    TrainComfortInfoDTO toTrainComfortInfoDTO(TrainInfo ticket);

    @Named("decimalToString")
    static String decimalToString(BigDecimal travelTime, @Context String language) {
        int hours = travelTime.intValue() / 60;
        int minutes = travelTime.intValue() % 60;
        return switch (language) {
            case ("ua") -> String.format("%sгод %sхв", hours, minutes);
            case ("eng") -> String.format("%sh %smin", hours, minutes);
            default -> String.format("%sгод %sхв", hours, minutes);
        };
    }

    @Named("departureTimeMapping")
    static String departureTimeMapping(String departureDate, @Context String language) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return switch (language) {
            case ("ua") -> {
                LocalDate date = LocalDate.parse(departureDate, formatter);
                formatter = DateTimeFormatter.ofPattern("dd.MM, E", new Locale("uk"));
                yield date.format(formatter);
            }
            case ("eng") -> {
                LocalDate date = LocalDate.parse(departureDate, formatter);
                formatter = DateTimeFormatter.ofPattern("dd.MM, E", new Locale("en"));
                yield date.format(formatter);
            }
            default -> throw new UndefinedLanguageException();
        };
    }

    @AfterMapping
    default void getPrice(TrainTicket ticket, @MappingTarget TicketDto ticketDTO) {
        ticketDTO.setPrice(ticket.getPrice());
    }


}
