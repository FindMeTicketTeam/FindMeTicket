package com.booking.app.controller;

import com.booking.app.dto.CityDto;
import com.booking.app.enums.ContentLanguage;
import com.booking.app.exception.ErrorDetails;
import com.booking.app.services.TypeAheadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.booking.app.constant.ApiMessagesConstants.INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE;

/**
 * Controller handling type-ahead functionality for city search.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Typing ahead", description = "Endpoint for the type-ahead feature for the search field")
public class TypeAheadController {

    private final TypeAheadService typeAheadService;

    @GetMapping(path = "/cities/typeahead")
    @Operation(summary = "Type ahead feature",
            description = "Find cities based on type-ahead search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful operation. Returns a list of cities",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CityDto.class)), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400"
                    , description = INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE + " OR " + "Required parameter 'startLetters' is not present.",
                    content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)})
    })
    public ResponseEntity<List<CityDto>> getCities(@RequestParam("startLetters") String startLetters,
                                                   @RequestHeader(name = HttpHeaders.CONTENT_LANGUAGE) @Parameter(required = true, description = "Content Language", schema = @Schema(type = "string", allowableValues = {"eng", "ua"})) ContentLanguage language) throws IOException {
        return ResponseEntity.ok().body(typeAheadService.findMatches(startLetters, language.getLanguage()));
    }

}
