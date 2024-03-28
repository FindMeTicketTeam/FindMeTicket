package com.booking.app.controller;

import com.booking.app.constant.JwtTokenConstants;
import com.booking.app.controller.api.TicketApi;
import com.booking.app.dto.RequestSortedTicketsDTO;
import com.booking.app.dto.RequestTicketsDTO;
import com.booking.app.dto.TicketDto;
import com.booking.app.exception.exception.UndefinedLanguageException;
import com.booking.app.services.SearchHistoryService;
import com.booking.app.services.SortTicketsService;
import com.booking.app.services.TicketService;
import com.booking.app.services.impl.SearchHistoryServiceImpl;
import com.booking.app.services.impl.scrape.ScraperManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
@RequiredArgsConstructor
public class TicketController implements TicketApi {

    private final ScraperManager scrapingService;

    private final SortTicketsService sortTicketsService;

    private final TicketService ticketService;

    private final SearchHistoryService searchHistoryService;

    @PostMapping("/searchTickets")
    @Override
    public ResponseBodyEmitter findTickets(@RequestBody RequestTicketsDTO ticketsDTO, @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) String siteLanguage, HttpServletResponse response, HttpServletRequest request) throws IOException, ParseException {

        validateLanguage(siteLanguage);

        searchHistoryService.addToHistory(ticketsDTO, siteLanguage, request);

        SseEmitter emitter = new SseEmitter();

        CompletableFuture<Boolean> isTicketScraped = scrapingService.scrapeTickets(ticketsDTO, emitter, siteLanguage);

        isTicketScraped.thenAccept(isFound -> {
            if (isFound) response.setStatus(HttpStatus.OK.value());
            else response.setStatus(HttpStatus.NOT_FOUND.value());
        });
        return emitter;
    }

    @GetMapping("/get/ticket/{id}")
    @Override
    public ResponseBodyEmitter getTicketById(@PathVariable UUID id, @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) String siteLanguage, HttpServletResponse response) throws IOException, ParseException {
        validateLanguage(siteLanguage);
        SseEmitter emitter = new SseEmitter();
        CompletableFuture<Boolean> isTicketFound = scrapingService.getTicket(id, emitter, siteLanguage);

        isTicketFound.thenAccept(isFound -> response.setStatus(isFound ? HttpStatus.OK.value() : HttpStatus.NOT_FOUND.value()));
        return emitter;
    }

    @PostMapping(value = "/sortedBy", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<?> getSortedTickets(@RequestBody RequestSortedTicketsDTO requestSortedTicketsDTO, @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) String siteLanguage, HttpServletRequest request) {
        validateLanguage(siteLanguage);
        return ResponseEntity.ok().body(sortTicketsService.getSortedTickets(requestSortedTicketsDTO, siteLanguage));
    }

    @PostMapping(value = "/selectedTransport", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TicketDto>> getSelectedTransportTicket(@RequestBody RequestTicketsDTO requestTicketsDTO) throws IOException {
        return ticketService.getBusTickets(requestTicketsDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void validateLanguage(String language) {
        if (language.equals("ua") || language.equals("eng")) return;
        throw new UndefinedLanguageException("Incomprehensible language passed into " + HttpHeaders.CONTENT_LANGUAGE + " (ua or eng required)");
    }

}
