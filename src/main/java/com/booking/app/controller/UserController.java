package com.booking.app.controller;

import com.booking.app.annotation.GlobalApiResponses;
import com.booking.app.constant.ApiMessagesConstants;
import com.booking.app.constant.RegistrationConstantMessages;
import com.booking.app.dto.*;
import com.booking.app.entity.User;
import com.booking.app.enums.ContentLanguage;
import com.booking.app.exception.ErrorDetails;
import com.booking.app.services.NotificationService;
import com.booking.app.services.PasswordService;
import com.booking.app.services.ReviewService;
import com.booking.app.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.booking.app.constant.PasswordConstantMessages.*;
import static com.booking.app.constant.RegistrationConstantMessages.*;
import static com.booking.app.exception.exception.InvalidConfirmationCodeException.MESSAGE_INVALID_CONFIRMATION_CODE_IS_PROVIDED;

/**
 * REST controller for managing user-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User", description = "Endpoints for user management")
@GlobalApiResponses
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final PasswordService passwordService;
    private final ReviewService reviewService;

    /**
     * Confirms the user's email using a code.
     *
     * @param dto the code confirmation data transfer object
     * @return a ResponseEntity containing the result of the confirmation
     */
    @PostMapping("/verify")
    @Operation(summary = "Email confirmation", description = "Confirm user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = USER_IS_VERIFIED_MESSAGE),
            @ApiResponse(responseCode = "400",
                    description = CODE_IS_NOT_RIGHT_MESSAGE,
                    content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "404",
                    description = THE_SPECIFIED_EMAIL_IS_NOT_REGISTERED_MESSAGE,
                    content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)})
    })
    public ResponseEntity<?> confirmEmailCode(@RequestBody @NotNull @Valid CodeConfirmationDto dto) {
        userService.confirmCode(dto);
        return ResponseEntity.status(HttpStatus.OK).body(USER_IS_VERIFIED_MESSAGE);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Operation(summary = "Delete a user",
            description = "Deletes the authenticated user account",
            parameters = {
                    @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true, description = "Provide access JWT token",
                            schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                    @Parameter(name = "RefreshToken", in = ParameterIn.COOKIE, required = true, description = "Provide refresh JWT token",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = ApiMessagesConstants.USER_HAS_BEEN_DELETED_MESSSAGE, content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = ApiMessagesConstants.UNAUTHENTICATED_MESSAGE, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDetails.class)))
            })
    public void deleteUser(@PathVariable("userId") String userId,
                           @AuthenticationPrincipal User user,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        userService.delete(user);
        CookieManager.deleteCookies(request, response);
    }

    @GetMapping("/{userId}/history")
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Operation(summary = "User history",
            description = "Routes from user history",
            parameters = {
                    @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true, description = "Provide access JWT token",
                            schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                    @Parameter(name = "RefreshToken", in = ParameterIn.COOKIE, required = true, description = "Provide refresh JWT token",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned", content = @Content(schema = @Schema(implementation = HistoryDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "401", description = ApiMessagesConstants.UNAUTHENTICATED_MESSAGE, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDetails.class)))
            })
    public ResponseEntity<?> getHistory(@PathVariable("userId") String userId,
                                        @RequestHeader(name = HttpHeaders.CONTENT_LANGUAGE) ContentLanguage language,
                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(userService.getHistory(user, language.getLanguage()));
    }

    @GetMapping("/{userId}/notifications/on")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Operation(summary = "Enable getting notifications",
            parameters = {
                    @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true, description = "Provide access JWT token",
                            schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                    @Parameter(name = "RefreshToken", in = ParameterIn.COOKIE, required = true, description = "Provide refresh JWT token",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            }, responses = {
            @ApiResponse(responseCode = "204", description = "Notifications are on"),
            @ApiResponse(responseCode = "401", description = ApiMessagesConstants.UNAUTHENTICATED_MESSAGE)
    })
    public void enableNotification(@PathVariable("userId") String userId,
                                   @AuthenticationPrincipal User user) {
        notificationService.enable(user);
    }

    @GetMapping("/{userId}/notifications/off")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Operation(summary = "Disable getting notifications",
            parameters = {
                    @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true, description = "Provide access JWT token",
                            schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                    @Parameter(name = "RefreshToken", in = ParameterIn.COOKIE, required = true, description = "Provide refresh JWT token",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Notifications are off"),
                    @ApiResponse(responseCode = "401", description = ApiMessagesConstants.UNAUTHENTICATED_MESSAGE)
            })
    public void disableNotification(@PathVariable("userId") String userId,
                                    @AuthenticationPrincipal User user) {
        notificationService.disable(user);
    }

    @PatchMapping("/{userId}/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Create new password",
            description = "Confirm code and set new password",
            responses = {
                    @ApiResponse(responseCode = "200", description = MESSAGE_PASSWORD_HAS_BEEN_SUCCESSFULLY_RESET, content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "400", description = MESSAGE_INVALID_CONFIRMATION_CODE_IS_PROVIDED, content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
                    @ApiResponse(responseCode = "404", description = RegistrationConstantMessages.THE_SPECIFIED_EMAIL_IS_NOT_REGISTERED_MESSAGE, content = {@Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)})
            })
    public ResponseEntity<?> confirmResetPassword(@PathVariable("userId") String userId,
                                                  @RequestBody @NotNull @Valid PasswordDto dto) {
        passwordService.resetPassword(dto);
        return ResponseEntity.ok(MESSAGE_PASSWORD_HAS_BEEN_SUCCESSFULLY_RESET);
    }

    @PatchMapping("/{userId}/password/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Operation(summary = "Update password",
            description = "Create new password for logged-in user",
            parameters = {
                    @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true, description = "Provide access JWT token",
                            schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                    @Parameter(name = "RefreshToken", in = ParameterIn.COOKIE, required = true, description = "Provide refresh JWT token",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = MESSAGE_NEW_PASSWORD_HAS_BEEN_CREATED, content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = ApiMessagesConstants.UNAUTHENTICATED_MESSAGE, content = @Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "400", description = MESSAGE_CURRENT_PASSWORD_IS_NOT_RIGHT, content = @Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    public void updatePassword(@PathVariable("userId") String userId,
                               @RequestBody @NotNull @Valid RequestUpdatePasswordDTO dto,
                               @AuthenticationPrincipal User user) {
        passwordService.changePassword(dto, user);
    }

    @PostMapping("/{userId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Operation(summary = "Add review",
            description = "Adds user review",
            parameters = {
                    @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true, description = "Provide access JWT token",
                            schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                    @Parameter(name = "RefreshToken", in = ParameterIn.COOKIE, required = true, description = "Provide refresh JWT token",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            },
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Saves and returns review",
                            content = {@Content(schema = @Schema(implementation = ReviewDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),

                    @ApiResponse(responseCode = "401", description = ApiMessagesConstants.UNAUTHENTICATED_MESSAGE, content = @Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    public ReviewDto saveReview(@PathVariable("userId") String userId,
                                @RequestBody @NotNull @Valid SaveReviewDto saveReviewDto,
                                @AuthenticationPrincipal User user) {
//        return reviewService.saveReview(saveReviewDto, user);
        return null;
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get all reviews",
            description = "Gets all users reviews",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Successfully returned all reviews",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewDto.class)), mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    public List<ReviewDto> getAllReviews() {
        return reviewService.getReviewList();
    }

    @DeleteMapping("/{userId}/reviews")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Operation(summary = "Delete review",
            description = "Deletes review of authorized user",
            parameters = {
                    @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true, description = "Provide access JWT token",
                            schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                    @Parameter(name = "RefreshToken", in = ParameterIn.COOKIE, required = true, description = "Provide refresh JWT token",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deleted"),
                    @ApiResponse(responseCode = "401", description = ApiMessagesConstants.UNAUTHENTICATED_MESSAGE, content = @Content(schema = @Schema(implementation = ErrorDetails.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    public void deleteReview(@PathVariable("userId") String userId,
                             @AuthenticationPrincipal User user) {
//        return reviewService.deleteReview(user);
    }

    @GetMapping("/{userId}/reviews")
    @Operation(summary = "Get user review",
            description = "Gets authorized user review",
            parameters = {
                    @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true, description = "Provide access JWT token",
                            schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                    @Parameter(name = "RefreshToken", in = ParameterIn.COOKIE, required = true, description = "Provide refresh JWT token",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            },
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Successfully returned review",
                            content = {@Content(schema = @Schema(implementation = ReviewDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
                    @ApiResponse(responseCode = "404", description = "Review is not found")
            }
    )
    public ReviewDto getReview(@PathVariable("userId") String userId,
                               @AuthenticationPrincipal User user) {
//        return reviewService.getUserReview(user);
        return null;
    }

}
