package com.booking.app.controller;

import com.booking.app.annotation.GlobalApiResponses;
import com.booking.app.constant.RegistrationConstantMessages;
import com.booking.app.dto.EmailDto;
import com.booking.app.enums.ContentLanguage;
import com.booking.app.exception.ErrorDetails;
import com.booking.app.services.MailSenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.booking.app.constant.ApiMessagesConstants.INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE;
import static com.booking.app.constant.PasswordConstantMessages.MESSAGE_CODE_HAS_BEEN_SENT;
import static com.booking.app.constant.RegistrationConstantMessages.CONFIRM_CODE_HAS_BEEN_SENT_ONE_MORE_TIME_MESSAGE;
import static com.booking.app.constant.RegistrationConstantMessages.THE_SPECIFIED_EMAIL_IS_NOT_REGISTERED_MESSAGE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Email verifier", description = "Verify user identity by sending code")
@GlobalApiResponses
public class UserVerifierController {

    private final MailSenderService mailSenderService;

    @PostMapping("/reset-code/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Send a reset code",
            description = "Sends reset code to verify user to change a password",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = MESSAGE_CODE_HAS_BEEN_SENT,
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "400", description = INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE + " OR " + "Invalid request body", content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
                    @ApiResponse(responseCode = "404",
                            description = RegistrationConstantMessages.THE_SPECIFIED_EMAIL_IS_NOT_REGISTERED_MESSAGE,
                            content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)})
            })
    public void sendResetCode(@RequestBody @NotNull @Valid EmailDto dto,
                              @RequestHeader(HttpHeaders.CONTENT_LANGUAGE) @Parameter(required = true, description = "Content Language", schema = @Schema(type = "string", allowableValues = {"eng", "ua"})) ContentLanguage language) {
        mailSenderService.sendResetCode(dto.getEmail(), language.getLanguage());
    }

    @PostMapping("/verification-code/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Send a verification code",
            description = "Sends a code to verify a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = CONFIRM_CODE_HAS_BEEN_SENT_ONE_MORE_TIME_MESSAGE),
            @ApiResponse(responseCode = "400", description = INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE + " OR " + "Invalid request body", content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "404",
                    description = THE_SPECIFIED_EMAIL_IS_NOT_REGISTERED_MESSAGE,
                    content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)})
    })
    public void resendConfirmEmailCode(@RequestHeader(HttpHeaders.CONTENT_LANGUAGE) @Parameter(required = true, description = "Content Language", schema = @Schema(type = "string", allowableValues = {"eng", "ua"})) ContentLanguage language,
                                       @RequestBody @NotNull @Valid EmailDto dto) {
        mailSenderService.sendVerificationCode(dto.getEmail(), language.getLanguage());
    }

}
