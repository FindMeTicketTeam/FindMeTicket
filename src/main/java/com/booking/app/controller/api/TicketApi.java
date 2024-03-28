package com.booking.app.controller.api;

import com.booking.app.dto.RequestSortedTicketsDTO;
import com.booking.app.dto.RequestTicketsDTO;
import com.booking.app.dto.TicketDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@Validated
@Tag(name = "Scraping info", description = "Endpoints for scraping tickets' info etc")
public interface TicketApi {

    @Operation(summary = "Searching tickets", description = "Find tickets based by criteria")
    @ApiResponse(responseCode = "200", description = "Returns tickets")
    @ApiResponse(responseCode = "404", description = "Tickets not found")
    ResponseBodyEmitter findTickets(@NotNull @Valid RequestTicketsDTO ticketsDTO, @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) String siteLanguage, HttpServletResponse response, HttpServletRequest request) throws IOException, ParseException;

    @Operation(summary = "Single ticket", description = "Ticket by ID")
    @ApiResponse(responseCode = "200", description = "Returns ticket if found")
    ResponseBodyEmitter getTicketById(@PathVariable UUID id, @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) String siteLanguage, HttpServletResponse response) throws IOException, ParseException;

    @Operation(summary = "Sorting", description = "Either by price, travel time, departure, or arrival")
    @ApiResponse(responseCode = "200", description = "Returns sorted tickets")
    ResponseEntity<?> getSortedTickets(@NotNull @Valid RequestSortedTicketsDTO requestSortedTicketsDTO, @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) String siteLanguage, HttpServletRequest request);

    @Operation(summary = "Tickets", description = "After tickets are uploaded, there's a need to switch between all transport types instantly without waiting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chosen ticket's type returned to the client"),
            @ApiResponse(responseCode = "404", description = "No tickets of the requested type were found")
    })
    ResponseEntity<List<TicketDto>> getSelectedTransportTicket(@NotNull @Valid RequestTicketsDTO requestSortedTicketsDTO) throws IOException;
}
