package com.booking.app.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Details about search history")
public class HistoryDto {

    @NotNull
    @Schema(description = "City of departure", example = "New York")
    private String departureCity;

    @NotNull
    @Schema(description = "City of arrival", example = "Los Angeles")
    private String arrivalCity;

    @NotNull
    @Schema(description = "Date of departure in YYYY-MM-DD format", example = "2023-04-29")
    private String departureDate;

    @NotNull
    @Schema(description = "Time when the entry was added", example = "2024-01-01T10:00:00Z")
    private String addingTime;

    @NotNull
    @Schema(description = "Indicates if the travel is by bus", example = "true")
    private Boolean bus;

    @NotNull
    @Schema(description = "Indicates if the travel is by train", example = "true")
    private Boolean train;

    @NotNull
    @Schema(description = "Indicates if the travel is by airplane", example = "false")
    private Boolean airplane;

    @NotNull
    @Schema(description = "Indicates if the travel is by ferry", example = "false")
    private Boolean ferry;
}
