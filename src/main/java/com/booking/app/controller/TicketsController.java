package com.booking.app.controller;

import com.booking.app.annotation.GlobalApiResponses;
import com.booking.app.dto.RequestSortedTicketsDto;
import com.booking.app.dto.RequestTicketsDto;
import com.booking.app.dto.TicketDto;
import com.booking.app.entity.User;
import com.booking.app.enums.ContentLanguage;
import com.booking.app.exception.ErrorDetails;
import com.booking.app.services.SortTicketsService;
import com.booking.app.services.TicketService;
import com.booking.app.services.UserService;
import com.booking.app.services.impl.scrape.ScraperManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.booking.app.constant.ApiMessagesConstants.INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE;
import static com.booking.app.constant.ApiMessagesConstants.INVALID_REQUEST_BODY_FORMAT_OR_VALIDATION_ERROR_MESSAGE;

@RestController
@RequestMapping(path = "/tickets", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
@RequiredArgsConstructor
@Tag(name = "Ticket", description = "Ticket management")
@GlobalApiResponses
public class TicketsController {

    private final ScraperManager scrapingService;

    private final SortTicketsService sortTicketsService;

    private final TicketService ticketService;

    private final UserService userService;

    @GetMapping("/search")
    @Operation(summary = "Search tickets",
            description = "Searches tickets by criteria",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tickets has been found", content = @Content(schema = @Schema(implementation = TicketDto.class), mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)),
                    @ApiResponse(responseCode = "400", description = INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE + " " + INVALID_REQUEST_BODY_FORMAT_OR_VALIDATION_ERROR_MESSAGE, content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
                    @ApiResponse(responseCode = "404", description = "Tickets are not found", content = @Content(schema = @Schema(hidden = true)))
            })
    public ResponseBodyEmitter findTickets(@RequestBody @NotNull @Valid RequestTicketsDto requestTicketsDto,
                                           @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) @Parameter(required = true, description = "Content Language", schema = @Schema(type = "string", allowableValues = {"eng", "ua"})) ContentLanguage language,
                                           @AuthenticationPrincipal User user,
                                           HttpServletResponse response) throws IOException, ParseException {
        userService.addHistory(requestTicketsDto, language.getLanguage(), user);

        SseEmitter emitter = new SseEmitter();
        CompletableFuture<Boolean> isTicketScraped = scrapingService.findTickets(requestTicketsDto, emitter, language.getLanguage());

        isTicketScraped.thenAccept(isFound -> {
            if (Boolean.TRUE.equals(isFound)) {
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        });
        return emitter;
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket info",
            description = "Gets ticket by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Ticket has been found", content = @Content(schema = @Schema(implementation = TicketDto.class), mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)),
            @ApiResponse(responseCode = "400", description = INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE + " " + INVALID_REQUEST_BODY_FORMAT_OR_VALIDATION_ERROR_MESSAGE, content = @Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Ticket is not found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseBodyEmitter getTicketById(@PathVariable("ticketId") UUID id,
                                             @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) @Parameter(required = true, description = "Content Language", schema = @Schema(type = "string", allowableValues = {"eng", "ua"})) ContentLanguage language,
                                             HttpServletResponse response) throws IOException, ParseException {
        SseEmitter emitter = new SseEmitter();
        CompletableFuture<Boolean> isTicketFound = scrapingService.getTicket(id, emitter, language.getLanguage());

        isTicketFound.thenAccept(isFound -> {
            if (Boolean.TRUE.equals(isFound)) {
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        });
        return emitter;
    }

    @GetMapping(value = "/sortBy", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sort tickets",
            description = "Either by price, travel time, departure, or arrival",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Get sorted tickets", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TicketDto.class)), mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "400", description = INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE + " " + INVALID_REQUEST_BODY_FORMAT_OR_VALIDATION_ERROR_MESSAGE, content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)})
            })
    public ResponseEntity<?> getSortedTickets(@RequestBody @NotNull @Valid RequestSortedTicketsDto requestSortedTicketsDto,
                                              @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) @Parameter(required = true, description = "Content Language", schema = @Schema(type = "string", allowableValues = {"eng", "ua"})) ContentLanguage language) {
        return ResponseEntity.ok().body(sortTicketsService.getSortedTickets(requestSortedTicketsDto, language.getLanguage()));
    }

    @PostMapping(value = "/transport", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get tickets", description = "Get tickets by type transport")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully returned"),
            @ApiResponse(responseCode = "404", description = "No tickets of the requested type were found")
    })
    public ResponseEntity<List<TicketDto>> getTicketByType(@RequestBody @NotNull @Valid RequestTicketsDto requestTicketsDTO) throws IOException {
        return ticketService.getTickets(requestTicketsDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
