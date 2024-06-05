package com.booking.app.services;

import com.booking.app.dto.RequestTicketsDto;
import com.booking.app.dto.TicketDto;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TicketService {

    /**
     * Retrieves bus OR/AND train tickets based on the provided request.
     *
     * @param dto the request containing filtering criteria
     * @param <T> the type of ticket DTO
     * @return a list of tickets or an empty optional if no tickets are found
     * @throws IOException if an I/O error occurs while determining the language
     */
    <T extends TicketDto> Optional<List<T>> getTickets(RequestTicketsDto dto) throws IOException;

}
