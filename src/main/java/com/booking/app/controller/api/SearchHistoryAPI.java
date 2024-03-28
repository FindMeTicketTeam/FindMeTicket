package com.booking.app.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface SearchHistoryAPI {

    @Operation(summary = "Searching history for user", description = "find search history by user id")
    @ApiResponse(responseCode = "200", description = "Returns history")
    @ApiResponse(responseCode = "404", description = "History not found")
    public ResponseEntity<?> getHistory(HttpServletRequest request);
}
