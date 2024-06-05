package com.booking.app.controller;

import com.booking.app.annotation.GlobalApiResponses;
import com.booking.app.constant.ApiMessagesConstants;
import com.booking.app.dto.AuthenticatedUserDto;
import com.booking.app.dto.LoginDto;
import com.booking.app.dto.SocialLoginDto;
import com.booking.app.enums.SocialProvider;
import com.booking.app.exception.ErrorDetails;
import com.booking.app.services.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.booking.app.constant.ApiMessagesConstants.INVALID_REQUEST_BODY_FORMAT_OR_VALIDATION_ERROR_MESSAGE;

/**
 * Controller responsible for handling login requests, both basic and social login.
 */
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Basic and socials login")
@GlobalApiResponses
public class LoginController {


    private final LoginService loginService;

    @PostMapping("/sign-in")
    @Operation(summary = "Basic authentication", description = "Authenticate by using email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = ApiMessagesConstants.USER_HAS_BEEN_AUTHENTICATED_MESSAGE,
                    content = {@Content(schema = @Schema(implementation = AuthenticatedUserDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "400",
                    description = INVALID_REQUEST_BODY_FORMAT_OR_VALIDATION_ERROR_MESSAGE,
                    content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401",
                    description = "Invalid credentials or such user doesn't exist",
                    content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)})
    })
    public ResponseEntity<?> basicLogin(@RequestBody @Valid @NotNull LoginDto dto,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        return loginService.loginWithEmailAndPassword(dto, request, response);
    }

    @PostMapping("/sign-in/{provider}")
    @Operation(summary = "Social authentication", description = "Authenticate by using Google or Facebook")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = ApiMessagesConstants.USER_HAS_BEEN_AUTHENTICATED_MESSAGE,
                    content = {@Content(schema = @Schema(implementation = AuthenticatedUserDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "400",
                    description = ApiMessagesConstants.NO_SOCIAL_IDENTITY_PROVIDERS_MESSAGE + " OR " + INVALID_REQUEST_BODY_FORMAT_OR_VALIDATION_ERROR_MESSAGE,
                    content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401",
                    description = ApiMessagesConstants.INVALID_CLIENT_PROVIDER_ID_MESSAGE,
                    content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
    })
    public ResponseEntity<?> socialLogin(@PathVariable("provider") @Parameter(required = true, description = "Provider type", schema = @Schema(type = "string", allowableValues = {"google", "facebook"})) SocialProvider provider,
                                         @RequestBody @Valid @NotNull SocialLoginDto dto,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (provider == SocialProvider.GOOGLE) {
            return loginService.loginWithGoogle(dto, request, response);
        } else if (request.getRequestURI().contains("facebook")) {
            // TODO complete authentication via Facebook
        }
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

}
