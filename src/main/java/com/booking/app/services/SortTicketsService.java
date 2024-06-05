package com.booking.app.services;

import com.booking.app.dto.RequestSortedTicketsDto;
import com.booking.app.dto.TicketDto;

import java.util.List;

public interface SortTicketsService {
    /**
     * Retrieves and sorts tickets based on the provided request DTO and language.
     *
     * @param dto      The request DTO containing the sorting and filtering criteria.
     * @param language The language for the ticket details.
     * @return A list of sorted ticket DTOs.
     */
    List<TicketDto> getSortedTickets(RequestSortedTicketsDto dto, String language);
}
