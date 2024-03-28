package com.booking.app.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

@Tag(name = "Notification", description = "On/off notifications")
public interface NotificationApi {

    @Operation(summary = "Disable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Off"),
            @ApiResponse(responseCode = "400", description = "Id identification is missed in cookie")
    })
    ResponseEntity<?> disableNotification(HttpServletRequest request);

    @Operation(summary = "Enable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "On"),
            @ApiResponse(responseCode = "400", description = "Id identification is missed in cookie")
    })
    ResponseEntity<?> enableNotification(HttpServletRequest request);
}
