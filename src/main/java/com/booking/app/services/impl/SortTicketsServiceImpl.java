package com.booking.app.services.impl;

import com.booking.app.dto.RequestSortedTicketsDto;
import com.booking.app.dto.TicketDto;
import com.booking.app.entity.ticket.Ticket;
import com.booking.app.entity.ticket.bus.BusTicket;
import com.booking.app.entity.ticket.train.TrainTicket;
import com.booking.app.mapper.BusMapper;
import com.booking.app.mapper.TrainMapper;
import com.booking.app.repositories.RouteRepository;
import com.booking.app.services.SortTicketsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SortTicketsServiceImpl implements SortTicketsService {

    private final RouteRepository routeRepository;

    private final BusMapper busMapper;

    private final TrainMapper trainMapper;

    /**
     * Creates a comparator based on the sorting criteria provided in the DTO.
     *
     * @param dto The request DTO containing the sorting criteria.
     * @return A comparator for sorting tickets.
     */
    @SneakyThrows
    private static Comparator<Ticket> customComparator(RequestSortedTicketsDto dto) {
        throw new OperationNotSupportedException();
//        return switch (dto.getSortingBy()) {
//            case PRICE_CRITERIA -> Comparator.comparing(Ticket::getPrice);
//            case DEPARTURE_TIME_CRITERIA -> Comparator.comparing(Ticket::getDepartureTime);
//            case ARRIVAL_TIME_CRITERIA -> Comparator.comparing(Ticket::formatArrivalDateTime);
//            case TRAVEL_TIME_CRITERIA -> Comparator.comparing(Ticket::getTravelTime);
//            default -> throw new UnsupportedOperationException("Any of the sorting criteria are not supportable");
//        };
    }

    /**
     * Sorts a list of tickets based on the provided comparator and sorting order.
     *
     * @param dto        The request DTO containing the sorting order.
     * @param tickets    The list of tickets to be sorted.
     * @param comparator The comparator to define the sorting logic.
     */
    private static void sortTickets(RequestSortedTicketsDto dto, List<Ticket> tickets, Comparator<Ticket> comparator) {
        tickets.sort(dto.isAscending()
                ? comparator
                : comparator.reversed());
    }

    public List<TicketDto> getSortedTickets(RequestSortedTicketsDto dto, String language) {
        List<Ticket> tickets = findTickets(dto);
        Comparator<Ticket> comparator = customComparator(dto);
        sortTickets(dto, tickets, comparator);

        return tickets.stream()
                .map(ticket -> mapTicketToDto(ticket, dto, language))
                .toList();
    }

    /**
     * Maps a ticket to its corresponding DTO based on the ticket type and the provided DTO.
     *
     * @param ticket   The ticket to be mapped.
     * @param dto      The request DTO containing the filtering criteria.
     * @param language The language for the ticket details.
     * @return The mapped ticket DTO, or null if the ticket type does not match the criteria.
     */
    private TicketDto mapTicketToDto(Ticket ticket, RequestSortedTicketsDto dto, String language) {
        return switch (ticket) {
            case BusTicket t when dto.getBus() -> busMapper.ticketToTicketDto(t, language);
            case TrainTicket t when dto.getTrain() -> trainMapper.toTrainTicketDto(t, language);
            default ->
                    throw new UnsupportedOperationException("Unsupported ticket type: " + ticket.getClass().getSimpleName());
        };
    }

    /**
     * Finds tickets based on the departure city, arrival city, and departure date.
     *
     * @param dto The request DTO containing the criteria for ticket search.
     * @return A list of tickets matching the criteria.
     */
    @NotNull
    private List<Ticket> findTickets(RequestSortedTicketsDto dto) {
        return new ArrayList<>(
                routeRepository.findByDepartureCityAndArrivalCityAndDepartureDate(
                        dto.getDepartureCity(),
                        dto.getArrivalCity(),
                        dto.getDepartureDate()
                ).getTickets());
    }

}
