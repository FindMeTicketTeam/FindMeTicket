package com.booking.app.controller;

import com.booking.app.annotation.GlobalApiResponses;
import com.booking.app.dto.RegistrationDTO;
import com.booking.app.enums.ContentLanguage;
import com.booking.app.exception.ErrorDetails;
import com.booking.app.services.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.booking.app.constant.ApiMessagesConstants.INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE;
import static com.booking.app.constant.RegistrationConstantMessages.EMAIL_IS_ALREADY_TAKEN_MESSAGE;
import static com.booking.app.constant.RegistrationConstantMessages.USER_REGISTERED_SUCCESSFULLY_MESSAGE;

/**
 * REST controller for handling user registration and email confirmation.
 */
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Tag(name = "Registration", description = "Endpoints for registration and confirmation")
@GlobalApiResponses
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * Registers a new user.
     *
     * @param language the language of the site
     * @param dto      the registration data transfer object
     * @throws MessagingException if an error occurs while sending the email
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Register a user",
            description = "Attempt to sign up new user",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = USER_REGISTERED_SUCCESSFULLY_MESSAGE + "\t\n"
                                    + EMAIL_IS_ALREADY_TAKEN_MESSAGE),
                    @ApiResponse(responseCode = "400", description = INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE + " OR " + "Invalid request body", content = @Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    public void signUp(@RequestHeader(HttpHeaders.CONTENT_LANGUAGE) @Parameter(required = true, description = "Content Language", schema = @Schema(type = "string", allowableValues = {"eng", "ua"})) ContentLanguage language,
                       @RequestBody @Valid @NotNull RegistrationDTO dto) throws
            MessagingException {
        registrationService.register(dto, language.getLanguage());
    }

}
