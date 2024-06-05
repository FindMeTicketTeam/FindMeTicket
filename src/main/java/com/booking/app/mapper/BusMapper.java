package com.booking.app.mapper;

import com.booking.app.dto.TicketDto;
import com.booking.app.entity.ticket.bus.BusTicket;
import com.booking.app.exception.exception.UndefinedLanguageException;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true))
public interface BusMapper {

    @Mapping(source = "route.departureCity", target = "departureCity")
    @Mapping(source = "route.arrivalCity", target = "arrivalCity")
    @Mapping(source = "route.departureDate", target = "departureDate", qualifiedByName = "departureTimeMapping")
    @Mapping(source = "travelTime", target = "travelTime", qualifiedByName = "decimalToString")
    @Mapping(target = "type", constant = "BUS")
    TicketDto ticketToTicketDto(BusTicket ticket, @Context String language);

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
    default void getPrice(BusTicket ticket, @MappingTarget TicketDto ticketDTO) {
        ticketDTO.setPrice(ticket.getPrice());
    }

}
